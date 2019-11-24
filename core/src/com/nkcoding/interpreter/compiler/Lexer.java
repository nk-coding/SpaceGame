package com.nkcoding.interpreter.compiler;

import java.util.LinkedList;

public class Lexer {
    private LinkedList<Token> tokens = new LinkedList<>();

    public void update(String code, boolean ignoreErrors) throws CompileException {
        String[] lines = code.split("\r?\n");
        for (int x = 0; x < lines.length; x++) {
            updateLine(lines[x], x, ignoreErrors);
        }
    }

    public void updateLine(String code, int line, boolean ignoreErrors) throws CompileException {
        char[] chars = code.toCharArray();

    }

    private void addToken(int id, String token, int line, int pos, int length) {
        //TODO implementation
    }
}
