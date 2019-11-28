package com.nkcoding.interpreter.compiler;

public class Token {

    public static final int BLOCK_START = 0x1;
    public static final int BLOCK_END = 0x2;
    public static final int LIST_START = 0x4;
    public static final int LIST_END = 0x8;
    public static final int BRACKET_START = 0x10;
    public static final int BRACKET_END = 0x20;
    public static final int DOT = 0x40;
    public static final int COMMA = 0x80;
    public static final int SEMICOLON = 0x100;
    public static final int EQUAL = 0x200;
    public static final int PLUS = 0x400;
    public static final int MINUS = 0x800;
    public static final int STAR = 0x1000;
    public static final int SLASH = 0x2000;
    public static final int PERCENT = 0x4000;
    public static final int IDENTIFIER = 0x8000;
    public static final int KEYWORD = 0x10000;
    public static final int STRING_LITERAL = 0x20000;
    public static final int INT_LITERAL = 0x40000;
    //public static final int FLOAT_LITERAL = 0x80000;
    public static final int COMMENT = 0x100000;
    public static final int NOT_DEFINED = 0x200000;
    /**
     * =+-/*%
     */
    public static final int OPERATOR = EQUAL | PLUS | MINUS | STAR | SLASH | PERCENT;


    private final String content;
    private final int type;
    private int line;
    private int pos;

    public Token(final String content, int type, int line, int pos) {
        this.content = content;
        this.type = type;
        this.line = line;
        this.pos = pos;
    }

    public String getContent() {
        return content;
    }

    public int getLine() {
        return line;
    }

    public int getPos() {
        return pos;
    }

    public int getLength() {
        return content.length();
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("[%s,%d, %d]", content, getLength(), pos);
    }
}
