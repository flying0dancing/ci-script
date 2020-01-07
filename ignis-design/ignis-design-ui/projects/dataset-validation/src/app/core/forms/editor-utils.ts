import { NgxMonacoEditorConfig } from 'ngx-monaco-editor';
import { Field, Schema } from '../../schemas';
import sparkSqlCompletionItems from './static/spark-sql-completion-items.json';
import IEditorConstructionOptions = monaco.editor.IEditorConstructionOptions;
import CompletionItem = monaco.languages.CompletionItem;
import IMonarchLanguage = monaco.languages.IMonarchLanguage;

export const JEXL_ID = 'jexl';
export const SPARK_SQL_ID = 'spark-sql';

const sparkSqlCompletionMap = new Map<monaco.editor.IModel, CompletionItem[]>();

export function registerSparkSql(): void {
  monaco.languages.register({ id: SPARK_SQL_ID });

  monaco.languages.registerCompletionItemProvider(SPARK_SQL_ID, {
    provideCompletionItems: (model: monaco.editor.IModel) =>
      sparkSqlCompletionMap.get(model)
  });

  monaco.languages.registerCompletionItemProvider(SPARK_SQL_ID, {
    provideCompletionItems: () => sparkSqlCompletionItems
  });
}

export function registerJexl(): void {
  monaco.languages.register({ id: JEXL_ID });

  monaco.languages.setLanguageConfiguration(JEXL_ID, {
    wordPattern: /(-?\d*\.\d\w*)|([^\`\~\!\@\#\%\^\&\*\(\)\-\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g,

    comments: {
      lineComment: '//',
      blockComment: ['/*', '*/']
    },

    brackets: [['{', '}'], ['[', ']'], ['(', ')']],

    onEnterRules: [
      {
        // e.g. /** | */
        beforeText: /^\s*\/\*\*(?!\/)([^\*]|\*(?!\/))*$/,
        afterText: /^\s*\*\/$/,
        action: {
          indentAction: monaco.languages.IndentAction.IndentOutdent,
          appendText: ' * '
        }
      },
      {
        // e.g. /** ...|
        beforeText: /^\s*\/\*\*(?!\/)([^\*]|\*(?!\/))*$/,
        action: {
          indentAction: monaco.languages.IndentAction.None,
          appendText: ' * '
        }
      },
      {
        // e.g.  * ...|
        beforeText: /^(\t|(\ \ ))*\ \*(\ ([^\*]|\*(?!\/))*)?$/,
        action: {
          indentAction: monaco.languages.IndentAction.None,
          appendText: '* '
        }
      },
      {
        // e.g.  */|
        beforeText: /^(\t|(\ \ ))*\ \*\/\s*$/,
        action: {
          indentAction: monaco.languages.IndentAction.None,
          removeText: 1
        }
      }
    ],

    autoClosingPairs: [
      { open: '{', close: '}' },
      { open: '[', close: ']' },
      { open: '(', close: ')' },
      { open: '"', close: '"', notIn: ['string'] },
      { open: "'", close: "'", notIn: ['string', 'comment'] },
      { open: '`', close: '`', notIn: ['string', 'comment'] },
      { open: '/**', close: ' */', notIn: ['string'] }
    ],

    folding: {
      markers: {
        start: new RegExp('^\\s*//\\s*#?region\\b'),
        end: new RegExp('^\\s*//\\s*#?endregion\\b')
      }
    }
  });

  monaco.languages.setMonarchTokensProvider(JEXL_ID, <IMonarchLanguage>{
    // Set defaultToken to invalid to see what you do not tokenize yet
    defaultToken: 'invalid',
    tokenPostfix: '.jexl',

    keywords: [
      'null',
      'new',
      'var',
      'empty',
      'size',
      'function',
      'return',
      'if',
      'else',
      'true',
      'false',
      'for',
      'while',
      'break',
      'continue'
    ],
    typeKeywords: [],

    operators: [
      'and',
      'or',
      'not',
      '!',
      '&',
      '|',
      '^',
      '~',
      '?',
      ':',
      '?:',
      '==',
      'eq',
      '!=',
      'ne',
      '<',
      'lt',
      '<=',
      'le',
      '>',
      'gt',
      '>=',
      'ge',
      '=~',
      '=^',
      '=$',
      '!$',
      '..',
      '+',
      '-',
      '*',
      '/',
      'div',
      '%',
      'mod',
      '+=',
      '-=',
      '*=',
      '/=',
      '%=',
      '&=',
      '|=',
      '^='
    ],
    symbols: /[=><!~?:&|+\-*\/\^%]+/,
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
    digits: /\d+(_+\d+)*/,

    regexpctl: /[(){}\[\]\$\^|\-*+?\.]/,
    regexpesc: /\\(?:[bBdDfnrstvwWn0\\\/]|@regexpctl|c[A-Z]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4})/,

    // The main tokenizer for our languages
    tokenizer: {
      root: [[/[{}]/, 'delimiter.bracket'], { include: 'common' }],

      common: [
        // identifiers and keywords
        [
          /[a-z_$][\w$]*/,
          {
            cases: {
              '@typeKeywords': 'keyword',
              '@keywords': 'keyword',
              '@default': 'identifier'
            }
          }
        ],
        [/[A-Z][\w\$]*/, 'type.identifier'], // to show class names nicely
        // [/[A-Z][\w\$]*/, 'identifier'],

        // whitespace
        { include: '@whitespace' },

        // regular expression: ensure it is terminated before beginning (otherwise it is an opeator)
        [
          /\/(?=([^\\\/]|\\.)+\/([gimuy]*)(\s*)(\.|;|\/|,|\)|\]|\}|$))/,
          { token: 'regexp', bracket: '@open', next: '@regexp' }
        ],

        // delimiters and operators
        [/[()\[\]]/, '@brackets'],
        [/[<>](?!@symbols)/, '@brackets'],
        [
          /@symbols/,
          {
            cases: {
              '@operators': 'delimiter',
              '@default': ''
            }
          }
        ],

        // numbers
        [/(@digits)[fFbBeE]([\-+]?(@digits))?/, 'number.float'],
        [/(@digits)\.(@digits)([fFbBeE][\-+]?(@digits))?/, 'number.float'],
        [/(@digits)/, 'number'],

        // delimiter: after number because of .\d floats
        [/[;,.]/, 'delimiter'],

        // strings
        [/"([^"\\]|\\.)*$/, 'string.invalid'], // non-teminated string
        [/'([^'\\]|\\.)*$/, 'string.invalid'], // non-teminated string
        [/"/, 'string', '@string_double'],
        [/'/, 'string', '@string_single'],
        [/`/, 'string', '@string_backtick']
      ],

      whitespace: [
        [/[ \t\r\n]+/, ''],
        [/\/\*\*(?!\/)/, 'comment.doc', '@jexldoc'],
        [/\/\*/, 'comment', '@comment'],
        [/\/\/.*$/, 'comment']
      ],

      comment: [
        [/[^\/*]+/, 'comment'],
        [/\*\//, 'comment', '@pop'],
        [/[\/*]/, 'comment']
      ],

      jexldoc: [
        [/[^\/*]+/, 'comment.doc'],
        [/\*\//, 'comment.doc', '@pop'],
        [/[\/*]/, 'comment.doc']
      ],

      // We match regular expression quite precisely
      regexp: [
        [
          /(\{)(\d+(?:,\d*)?)(\})/,
          [
            'regexp.escape.control',
            'regexp.escape.control',
            'regexp.escape.control'
          ]
        ],
        [
          /(\[)(\^?)(?=(?:[^\]\\\/]|\\.)+)/,
          [
            'regexp.escape.control',
            { token: 'regexp.escape.control', next: '@regexrange' }
          ]
        ],
        [
          /(\()(\?:|\?=|\?!)/,
          ['regexp.escape.control', 'regexp.escape.control']
        ],
        [/[()]/, 'regexp.escape.control'],
        [/@regexpctl/, 'regexp.escape.control'],
        [/[^\\\/]/, 'regexp'],
        [/@regexpesc/, 'regexp.escape'],
        [/\\\./, 'regexp.invalid'],
        ['/', { token: 'regexp', bracket: '@close' }, '@pop']
      ],

      regexrange: [
        [/-/, 'regexp.escape.control'],
        [/\^/, 'regexp.invalid'],
        [/@regexpesc/, 'regexp.escape'],
        [/[^\]]/, 'regexp'],
        [/\]/, '@brackets.regexp.escape.control', '@pop']
      ],

      string_double: [
        [/[^\\"]+/, 'string'],
        [/@escapes/, 'string.escape'],
        [/\\./, 'string.escape.invalid'],
        [/"/, 'string', '@pop']
      ],

      string_single: [
        [/[^\\']+/, 'string'],
        [/@escapes/, 'string.escape'],
        [/\\./, 'string.escape.invalid'],
        [/'/, 'string', '@pop']
      ],

      string_backtick: [
        [/\$\{/, { token: 'delimiter.bracket', next: '@bracketCounting' }],
        [/[^\\`$]+/, 'string'],
        [/@escapes/, 'string.escape'],
        [/\\./, 'string.escape.invalid'],
        [/`/, 'string', '@pop']
      ],

      bracketCounting: [
        [/\{/, 'delimiter.bracket', '@bracketCounting'],
        [/\}/, 'delimiter.bracket', '@pop'],
        { include: 'common' }
      ]
    }
  });
}

export const DEFAULT_OPTIONS: IEditorConstructionOptions = {
  language: JEXL_ID,
  autoIndent: true,
  automaticLayout: true,
  minimap: {
    enabled: false
  },
  fontFamily: '"Roboto Mono", monospace',
  fontSize: 16,
  lineHeight: 20,
  scrollBeyondLastLine: false,
  lineNumbersMinChars: 2,
  codeLens: false
};

export function registerLanguages() {
  registerJexl();
  registerSparkSql();
}

export const MONACO_CONFIG = {
  defaultOptions: DEFAULT_OPTIONS,
  onMonacoLoad: registerLanguages
} as NgxMonacoEditorConfig;

export class EditorUtils {
  static jexlId = JEXL_ID;

  static sparkSqlId = SPARK_SQL_ID;

  static defaultConfig = MONACO_CONFIG;

  static createFieldCompletionItemProvider(field: Field): CompletionItem {
    return {
      label: field.name,
      kind: monaco.languages.CompletionItemKind.Variable,
      detail: EditorUtils.createDetail(field),
      sortText: 'a',
      documentation: {
        isTrusted: true,
        value: EditorUtils.createDocumentation(field)
      }
    };
  }

  static createSchemaFieldCompletionItemProvider(
    schema: Schema,
    field: Field
  ): CompletionItem[] {
    const baseItem = {
      kind: monaco.languages.CompletionItemKind.Variable,
      detail: EditorUtils.createDetail(field),
      sortText: 'a',
      documentation: {
        isTrusted: true,
        value: EditorUtils.createDocumentation(field)
      }
    };

    return [
      { ...baseItem, label: field.name },
      { ...baseItem, label: `${schema.physicalTableName}.${field.name}` }
    ];
  }

  private static createDetail(field: Field) {
    const nullableFlag = field.nullable ? '?' : '';

    const scaleAndPrecision = field.precision
      ? ` (${field.precision},${field.scale})`
      : '';
    let lengthRange = '';
    if (field.minLength || field.maxLength) {
      lengthRange = ` [${field.minLength ? field.minLength : ''}..${
        field.maxLength ? field.maxLength : ''
      }]`;
    }
    const regExpression = field.regularExpression
      ? ` /${field.regularExpression}/`
      : '';
    const format = field.format ? ` "${field.format}"` : '';
    return `${field.name}: ${field.type}${nullableFlag}${scaleAndPrecision}${lengthRange}${regExpression}${format}`;
  }

  private static createDocumentation(field: Field) {
    const type = `- type: \`${field.type}\`\n`;
    const nullableFlag = `- nullable: ${
      field.nullable ? '**Yes**' : '`No`'
    } _– value required or not_\n`;

    const scaleAndPrecision = field.precision
      ? this.createPrecisionScaleDocs(field)
      : '';
    let lengthRange = '';
    if (field.minLength || field.maxLength) {
      lengthRange = this.createMinMaxDocs(field);
    }
    const regExpression = field.regularExpression
      ? `- regular expression: \`${field.regularExpression}\` _– required to match_\n`
      : '';
    const format = field.format
      ? `- date-time format: \`${field.format}\``
      : '';
    return `#### ${field.name}\n${type}${nullableFlag}${scaleAndPrecision}${lengthRange}${regExpression}${format}`;
  }

  private static createPrecisionScaleDocs(field: Field) {
    const precision = `- precision: \`${field.precision}\` _– max number of digits_`;
    const scale = `scale: \`${field.scale}\` _– required digits after decimal point_`;
    return `${precision}, ${scale}\n`;
  }

  private static createMinMaxDocs(field: Field) {
    let min = '';
    let max = '';
    if (field.minLength) {
      min = `min length: \`${field.minLength}\``;
    }
    if (field.maxLength) {
      max = `max length: \`${field.maxLength}\``;
    }
    return `- ${min}${min ? ',' : ''} ${max}\n`;
  }

  static setSparkSqlEditorInstanceFields(
    editorModel: monaco.editor.IModel,
    schemas: Schema[]
  ): void {
    const allCompletionItems = [];

    schemas.forEach(schema =>
      schema.fields.forEach(field => {
        const completionItems = EditorUtils.createSchemaFieldCompletionItemProvider(
          schema,
          field
        );
        completionItems.forEach(item => allCompletionItems.push(item));
      })
    );

    sparkSqlCompletionMap.set(editorModel, allCompletionItems);
  }

  static clearSparkSqlEditorInstanceFields(
    editorModel: monaco.editor.IModel
  ): void {
    sparkSqlCompletionMap.delete(editorModel);
  }

  static createEditorHeight(steps: number, includePx = true): string | number {
    const offsetTop = 2;
    const height =
      steps * EditorUtils.defaultConfig.defaultOptions.lineHeight + offsetTop;

    return includePx ? height + 'px' : height;
  }
}
