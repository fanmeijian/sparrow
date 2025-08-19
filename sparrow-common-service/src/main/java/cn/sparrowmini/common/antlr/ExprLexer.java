// Generated from Expr.g4 by ANTLR 4.13.2
package cn.sparrowmini.common.antlr;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class ExprLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, AND=12, OR=13, NOT=14, IS=15, NULL=16, IN=17, ID=18, 
		STRING=19, NUMBER=20, WS=21;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "AND", "OR", "NOT", "IS", "NULL", "IN", "ID", "STRING", 
			"NUMBER", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'.'", "'='", "'!='", "'<'", "'<='", "'>'", "'>='", 
			"'like'", "','", "'and'", "'or'", "'not'", "'is'", "'null'", "'in'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"AND", "OR", "NOT", "IS", "NULL", "IN", "ID", "STRING", "NUMBER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public ExprLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Expr.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u0015\u0083\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001"+
		"\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0005\u0011`\b"+
		"\u0011\n\u0011\f\u0011c\t\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0005\u0012i\b\u0012\n\u0012\f\u0012l\t\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0004\u0013q\b\u0013\u000b\u0013\f\u0013r\u0001\u0013"+
		"\u0001\u0013\u0004\u0013w\b\u0013\u000b\u0013\f\u0013x\u0003\u0013{\b"+
		"\u0013\u0001\u0014\u0004\u0014~\b\u0014\u000b\u0014\f\u0014\u007f\u0001"+
		"\u0014\u0001\u0014\u0000\u0000\u0015\u0001\u0001\u0003\u0002\u0005\u0003"+
		"\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n\u0015"+
		"\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011#\u0012"+
		"%\u0013\'\u0014)\u0015\u0001\u0000\u0005\u0003\u0000AZ__az\u0004\u0000"+
		"09AZ__az\u0002\u0000\'\'\\\\\u0001\u000009\u0003\u0000\t\n\r\r  \u0089"+
		"\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000"+
		"\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000"+
		"\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000"+
		"\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011"+
		"\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015"+
		"\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019"+
		"\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d"+
		"\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001"+
		"\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000"+
		"\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000"+
		"\u0001+\u0001\u0000\u0000\u0000\u0003-\u0001\u0000\u0000\u0000\u0005/"+
		"\u0001\u0000\u0000\u0000\u00071\u0001\u0000\u0000\u0000\t3\u0001\u0000"+
		"\u0000\u0000\u000b6\u0001\u0000\u0000\u0000\r8\u0001\u0000\u0000\u0000"+
		"\u000f;\u0001\u0000\u0000\u0000\u0011=\u0001\u0000\u0000\u0000\u0013@"+
		"\u0001\u0000\u0000\u0000\u0015E\u0001\u0000\u0000\u0000\u0017G\u0001\u0000"+
		"\u0000\u0000\u0019K\u0001\u0000\u0000\u0000\u001bN\u0001\u0000\u0000\u0000"+
		"\u001dR\u0001\u0000\u0000\u0000\u001fU\u0001\u0000\u0000\u0000!Z\u0001"+
		"\u0000\u0000\u0000#]\u0001\u0000\u0000\u0000%d\u0001\u0000\u0000\u0000"+
		"\'p\u0001\u0000\u0000\u0000)}\u0001\u0000\u0000\u0000+,\u0005(\u0000\u0000"+
		",\u0002\u0001\u0000\u0000\u0000-.\u0005)\u0000\u0000.\u0004\u0001\u0000"+
		"\u0000\u0000/0\u0005.\u0000\u00000\u0006\u0001\u0000\u0000\u000012\u0005"+
		"=\u0000\u00002\b\u0001\u0000\u0000\u000034\u0005!\u0000\u000045\u0005"+
		"=\u0000\u00005\n\u0001\u0000\u0000\u000067\u0005<\u0000\u00007\f\u0001"+
		"\u0000\u0000\u000089\u0005<\u0000\u00009:\u0005=\u0000\u0000:\u000e\u0001"+
		"\u0000\u0000\u0000;<\u0005>\u0000\u0000<\u0010\u0001\u0000\u0000\u0000"+
		"=>\u0005>\u0000\u0000>?\u0005=\u0000\u0000?\u0012\u0001\u0000\u0000\u0000"+
		"@A\u0005l\u0000\u0000AB\u0005i\u0000\u0000BC\u0005k\u0000\u0000CD\u0005"+
		"e\u0000\u0000D\u0014\u0001\u0000\u0000\u0000EF\u0005,\u0000\u0000F\u0016"+
		"\u0001\u0000\u0000\u0000GH\u0005a\u0000\u0000HI\u0005n\u0000\u0000IJ\u0005"+
		"d\u0000\u0000J\u0018\u0001\u0000\u0000\u0000KL\u0005o\u0000\u0000LM\u0005"+
		"r\u0000\u0000M\u001a\u0001\u0000\u0000\u0000NO\u0005n\u0000\u0000OP\u0005"+
		"o\u0000\u0000PQ\u0005t\u0000\u0000Q\u001c\u0001\u0000\u0000\u0000RS\u0005"+
		"i\u0000\u0000ST\u0005s\u0000\u0000T\u001e\u0001\u0000\u0000\u0000UV\u0005"+
		"n\u0000\u0000VW\u0005u\u0000\u0000WX\u0005l\u0000\u0000XY\u0005l\u0000"+
		"\u0000Y \u0001\u0000\u0000\u0000Z[\u0005i\u0000\u0000[\\\u0005n\u0000"+
		"\u0000\\\"\u0001\u0000\u0000\u0000]a\u0007\u0000\u0000\u0000^`\u0007\u0001"+
		"\u0000\u0000_^\u0001\u0000\u0000\u0000`c\u0001\u0000\u0000\u0000a_\u0001"+
		"\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000b$\u0001\u0000\u0000\u0000"+
		"ca\u0001\u0000\u0000\u0000dj\u0005\'\u0000\u0000ei\b\u0002\u0000\u0000"+
		"fg\u0005\\\u0000\u0000gi\t\u0000\u0000\u0000he\u0001\u0000\u0000\u0000"+
		"hf\u0001\u0000\u0000\u0000il\u0001\u0000\u0000\u0000jh\u0001\u0000\u0000"+
		"\u0000jk\u0001\u0000\u0000\u0000km\u0001\u0000\u0000\u0000lj\u0001\u0000"+
		"\u0000\u0000mn\u0005\'\u0000\u0000n&\u0001\u0000\u0000\u0000oq\u0007\u0003"+
		"\u0000\u0000po\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000rp\u0001"+
		"\u0000\u0000\u0000rs\u0001\u0000\u0000\u0000sz\u0001\u0000\u0000\u0000"+
		"tv\u0005.\u0000\u0000uw\u0007\u0003\u0000\u0000vu\u0001\u0000\u0000\u0000"+
		"wx\u0001\u0000\u0000\u0000xv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000"+
		"\u0000y{\u0001\u0000\u0000\u0000zt\u0001\u0000\u0000\u0000z{\u0001\u0000"+
		"\u0000\u0000{(\u0001\u0000\u0000\u0000|~\u0007\u0004\u0000\u0000}|\u0001"+
		"\u0000\u0000\u0000~\u007f\u0001\u0000\u0000\u0000\u007f}\u0001\u0000\u0000"+
		"\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0081\u0001\u0000\u0000"+
		"\u0000\u0081\u0082\u0006\u0014\u0000\u0000\u0082*\u0001\u0000\u0000\u0000"+
		"\b\u0000ahjrxz\u007f\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}