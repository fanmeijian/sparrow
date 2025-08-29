import { NgxMonacoEditorConfig } from "ngx-monaco-editor-v2";

export function onMonacoLoad() {
  const monaco = (window as any).monaco;
  console.log('Monaco loaded:', monaco);

  monaco.languages.register({ id: 'drools' });


  monaco.languages.setMonarchTokensProvider('drools', {
    keywords: [
      'rule', 'when', 'then', 'end', 'salience', 'agenda-group',
      'dialect', 'lock-on-active', 'no-loop', 'update', 'insert', 'retract', 'import', 'global'
    ],
    typeKeywords: ['String', 'Integer', 'Boolean', 'Double', 'Float', 'Long'],
    operators: ['=', '>', '<', '>=', '<=', '==', '!=', '->'],
    tokenizer: {
      root: [
        [/[{}]/, 'delimiter.bracket'],
        [/[a-z_$][\w$]*/, {
          cases: {
            '@keywords': 'keyword',
            '@typeKeywords': 'type',
            '@default': 'identifier'
          }
        }],
        [/[A-Z][\w\$]*/, 'type.identifier'],
        { include: '@whitespace' },
        [/\d+/, 'number'],
        [/"([^"\\]|\\.)*$/, 'string.invalid'],
        [/"/, 'string', '@string']
      ],
      whitespace: [
        [/[ \t\r\n]+/, ''],
        [/\/\*/, 'comment', '@comment'],
        [/\/\/.*$/, 'comment']
      ],
      comment: [
        [/[^\/*]+/, 'comment'],
        [/\*\//, 'comment', '@pop'],
        [/[\/*]/, 'comment']
      ],
      string: [
        [/[^\\"]+/, 'string'],
        [/\\./, 'string.escape'],
        [/"/, 'string', '@pop']
      ]
    }
  });

  monaco.languages.registerCompletionItemProvider('drools', {
    provideCompletionItems: (
      model: any,
      position: any,
      context: any,
      token: any
    ): any => {
      const keywords: any = [
        'when', 'then', 'end', 'agenda-group',
        'dialect', 'lock-on-active', 'no-loop', 'update', 'insert', 'retract', 'import', 'global'
      ]

      const suggestions1 = keywords.map((keyword: string) => ({
        label: keyword,
        kind: monaco.languages.CompletionItemKind.Keyword,
        insertText: keyword,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        documentation: `Drools keyword: ${keyword}`,
        range: getReplaceRange(model, position)
      }));
      return {


        suggestions: [
          {
            label: 'rule',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'rule "${1:name}"\nwhen\n\t${2:condition}\nthen\n\t${3:action}\nend',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Drools rule template',
            range: getReplaceRange(model, position)
          },
          {
            label: 'salience',
            kind: monaco.languages.CompletionItemKind.Keyword,
            insertText: 'salience ${1:10}',
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: 'Rule priority',
            range: getReplaceRange(model, position)
          }
        ].concat(suggestions1)
      };
    }
  });


  function getReplaceRange(model: any, position: any): any {
    const word = model.getWordUntilPosition(position);
    return {
      startLineNumber: position.lineNumber,
      endLineNumber: position.lineNumber,
      startColumn: word.startColumn,
      endColumn: word.endColumn
    };
  }

  // 你可以加上 theme 注册
  monaco.editor.defineTheme('droolsTheme', {
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'keyword', foreground: '0000FF' },
      { token: 'string', foreground: 'A31515' },
      { token: 'number', foreground: '098658' }
    ]
  });


}


export const monacoConfig: NgxMonacoEditorConfig = {
  baseUrl: 'assets',
  defaultOptions: { scrollBeyondLastLine: false },
  onMonacoLoad
};
