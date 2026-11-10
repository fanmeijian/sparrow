#!/usr/bin/env python3
"""Check the isolated v3 reactor without importing retired application modules."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}
parent = ET.parse(ROOT / 'sparrow-platform/pom.xml').getroot()
modules = {Path(n.text).name: (ROOT / 'sparrow-platform' / n.text).resolve()
           for n in parent.findall('m:modules/m:module', NS)}
errors, graph, types = [], {}, {}
pure = {'sparrow-security-api', 'sparrow-identity-api', 'sparrow-rbac-core',
        'sparrow-data-policy-core', 'sparrow-group-core', 'sparrow-storage-api'}
for name, directory in modules.items():
    pom = ET.parse(directory / 'pom.xml').getroot()
    graph[name] = []
    for dependency in pom.findall('m:dependencies/m:dependency', NS):
        value = lambda field: dependency.findtext('m:' + field, namespaces=NS)
        if value('scope') == 'test':
            continue
        group, artifact = value('groupId'), value('artifactId')
        if group == 'cn.sparrowmini':
            if artifact not in modules:
                errors.append(f'{name}: dependency outside v3 reactor: {artifact}')
            else:
                graph[name].append(artifact)
        if name in pure and group != 'cn.sparrowmini':
            errors.append(f'{name}: pure API/core has external runtime dependency {group}:{artifact}')
    for source in directory.glob('src/main/java/**/*.java'):
        text = source.read_text()
        package = re.search(r'\bpackage\s+([\w.]+)\s*;', text)
        if not package:
            errors.append(f'{source}: missing package')
            continue
        full_name = package.group(1) + '.' + source.stem
        if full_name in types:
            errors.append(f'duplicate type {full_name}: {types[full_name]}, {source}')
        types[full_name] = source
        if re.search(r'\b(createNativeQuery|nativeQuery\s*=\s*true|DriverManager|JdbcTemplate)\b', text):
            errors.append(f'{source}: unreviewed runtime native SQL/JDBC')
        if re.search(r'\bimport\s+(cn\.sparrowmini\.common\.|org\.hibernate\.event\.)', text):
            errors.append(f'{source}: retired package or ORM event authorization dependency')
        if name in pure and re.search(r'\b(?:org\.springframework|jakarta\.persistence|org\.hibernate)\.', text):
            errors.append(f'{source}: pure API/core imports a framework')

visiting, visited = set(), set()
def visit(name):
    if name in visiting:
        errors.append(f'dependency cycle at {name}')
        return
    if name in visited:
        return
    visiting.add(name)
    for child in graph[name]:
        visit(child)
    visiting.remove(name)
    visited.add(name)
for name in modules:
    visit(name)
for name in ('sparrow-rbac-core', 'sparrow-rbac-jpa', 'sparrow-file-service', 'sparrow-system'):
    pending, seen = list(graph[name]), set()
    while pending:
        child = pending.pop()
        if child in seen:
            continue
        seen.add(child)
        if child.startswith('sparrow-data-policy'):
            errors.append(f'{name}: forces optional data-policy through {child}')
        pending.extend(graph[child])
if errors:
    print('\n'.join(errors), file=sys.stderr)
    raise SystemExit(1)
print(f'Architecture checks passed: {len(modules)} modules, {len(types)} production Java types, no cycles or retired runtime dependencies.')
