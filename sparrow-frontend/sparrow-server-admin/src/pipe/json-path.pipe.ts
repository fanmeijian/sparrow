// json-path.pipe.ts
import { Pipe, PipeTransform } from '@angular/core';
import * as jsonpath from 'jsonpath';

@Pipe({
  name: 'jsonPath'
})
export class JsonPathPipe implements PipeTransform {
  transform(obj: any, path: string): any {
    try {
      const result = jsonpath.query(obj, path);
      return result.length ? result[0] : null;
    } catch {
      return null;
    }
  }
}
