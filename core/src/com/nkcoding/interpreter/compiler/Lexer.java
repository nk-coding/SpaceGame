package com.nkcoding.interpreter.compiler;

import java.util.*;

public class Lexer {
    private TreeSet<Token> tokens = new TreeSet<>();

    public void update(String code, boolean ignoreErrors) throws CompileException {
        //reset tokens
        tokens.clear();
        String[] lines = code.split("\r?\n");
        for (int x = 0; x < lines.length; x++) {
            parseLine(lines[x], x, ignoreErrors);
        }
    }

    public void updateLine(String code, int line, boolean ignoreErrors) throws CompileException {
        tokens.removeIf(token -> token.getLine() == line);
        parseLine(code.replaceAll("\r?\n", ""), line, ignoreErrors);
    }

    private void parseLine(String code, int line, boolean ignoreErrors) throws CompileException {
        char[] chars = code.toCharArray();
        int pos = 0;
        while (pos < chars.length) {
            char c = chars[pos];
            if (!Character.isWhitespace(c)) {
                if (Character.isDigit(c)) {
                    //check for number
                    int startPos = pos;
                    boolean foundAll = false;
                    while (pos < chars.length && !foundAll) {
                        c = chars[pos];
                        if (Character.isDigit(c)) {
                            pos++;
                        } else {
                            pos--;
                            foundAll = true;
                        }
                    }
                    String token = code.substring(startPos, Math.min(pos + 1, code.length()));
                    addToken(Token.INT_LITERAL, token, line, startPos);
                } else if (Character.isAlphabetic(c) || c == '_') {
                    //identifier or keyword
                    int startPos = pos;
                    boolean foundAll = false;
                    while (pos < chars.length && !foundAll) {
                        c = chars[pos];
                        if (Character.isLetterOrDigit(c) || c == '_') {
                            pos++;
                        } else {
                            pos--;
                            foundAll = true;
                        }
                    }
                    String token = code.substring(startPos, Math.min(pos + 1, code.length()));
                    int type = CompilerHelper.isReservedKeyword(token) ? Token.KEYWORD : Token.IDENTIFIER;
                    addToken(type, token, line, startPos);
                } else if (c == '"') {
                    //String literal
                    int startPos = pos;
                    if (pos == chars.length - 1) {
                        if (!ignoreErrors) {
                            throw new CompileException("string literal has no end", new ProgramPosition(line, pos));
                        }
                        addToken(Token.STRING_LITERAL, "\"", line, pos);
                    } else {
                        pos++;
                        boolean escaped = false;
                        boolean foundAll = false;
                        while (pos < chars.length && !foundAll) {
                            c = chars[pos];
                            if (c == '\\') {
                                //check for escape
                                escaped = !escaped;
                                pos++;
                            } else if (c == '"') {
                                //check for escape
                                if (escaped) {
                                    pos++;
                                } else {
                                    foundAll = true;
                                }
                            } else {
                                //check for escape
                                if (escaped) {
                                    //check if the escape is legal
                                    if (c != 'n') {
                                        if (!ignoreErrors) {
                                            throw new CompileException("illegal escape sequence: \\"
                                                    + c, new ProgramPosition(line, pos));
                                        }
                                    }
                                }
                                pos++;
                            }
                        }
                        if (c != '"' && !ignoreErrors) {
                            throw new CompileException("string literal has no end", new ProgramPosition(line, pos));
                        }
                        addToken(Token.STRING_LITERAL, code.substring(startPos, Math.min(pos + 1, code.length())), line, startPos);
                    }
                } else if (c == '/') {
                    //comment or slash
                    if ((pos + 1) < chars.length && chars[pos + 1] == '/') {
                        //comment
                        addToken(Token.COMMENT, code.substring(pos), line, pos);
                        pos = chars.length;
                    } else {
                        //normal slash
                        addToken(Token.SLASH, "/", line, pos);
                    }
                } else if ("{}[]()=+-*%.,;".indexOf(c) > -1) {
                    //comment or a single thing
                    int type;
                    switch (c) {
                        case '{':
                            type = Token.BLOCK_START;
                            break;
                        case '}':
                            type = Token.BLOCK_END;
                            break;
                        case '[':
                            type = Token.LIST_START;
                            break;
                        case ']':
                            type = Token.LIST_END;
                            break;
                        case '(':
                            type = Token.BRACKET_START;
                            break;
                        case ')':
                            type = Token.BRACKET_END;
                            break;
                        case '=':
                            type = Token.EQUAL;
                            break;
                        case '+':
                            type = Token.PLUS;
                            break;
                        case '-':
                            type = Token.MINUS;
                            break;
                        case '*':
                            type = Token.STAR;
                            break;
                        case '%':
                            type = Token.PERCENT;
                            break;
                        case '.':
                            type = Token.DOT;
                            break;
                        case ',':
                            type = Token.COMMA;
                            break;
                        case ';':
                            type = Token.SEMICOLON;
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    addToken(type, String.valueOf(c), line, pos);
                } else {
                    //undefined
                    if (ignoreErrors) {
                        addToken(Token.NOT_DEFINED, String.valueOf(c), line, pos);
                    } else {
                        throw new CompileException("not defined token: " + c, new ProgramPosition(line, pos));
                    }
                }
            }

            pos++;
        }
    }

    private void addToken(int id, String token, int line, int pos) {
        tokens.add(new Token(token, id, line, pos));
    }

    public int getTokenCount() {
        return tokens.size();
    }

    /**
     * gets an Iterator which starts at the first Token for the specified line
     */
    public Iterator<Token> getLineStartIterator(int line) {
        return tokens.tailSet(getTokenAfter(line, 0)).iterator();
    }

    /**
     * get the first token at this position or earlier
     */
    public Token getTokenBefore(int line, int pos) {
        return tokens.floor(new Token("",0, line, pos));
    }

    /**
     * get the first token at this position or later
     */
    public Token getTokenAfter(int line, int pos) {
        return tokens.ceiling(new Token("",0, line, pos));
    }


    public Set<Token> getTokens() {
        return Collections.unmodifiableSet(tokens);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int lastLine = 0;
        for (Token token : tokens) {
            if (lastLine != token.getLine()) {
                lastLine = token.getLine();
                builder.append("\n");
            }
            builder.append(token.toString());
            builder.append(" ");
        }
        return builder.toString();
    }
}
