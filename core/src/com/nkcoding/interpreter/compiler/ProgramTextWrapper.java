package com.nkcoding.interpreter.compiler;

public class ProgramTextWrapper {
    private String[] lines;

    //where should the rest of the line be ignored?
    //-1 symbolizes that it should not be ignored at all
    private int[] ignoreAt;

    private ProgramPosition position;

    public ProgramPosition getPosition() {
        return position.getClone();
    }

    //at this time this is not checked, I will probably change this
    public void setPosition(ProgramPosition position) {
        this.position = position;
        jumpIfNecessary();
    }

    //throws an IllegalOperationException if it is at the start
    public void moveBackward() {
        if (position.getColumn() == 0) {
            if (position.getLine() == 0)
                throw new UnsupportedOperationException("can't move backwards at the beginning");
            //now at begin of line, move to last line last char
            position.setLine(position.getLine() - 1);
            position.setColumn(lines[position.getLine()].length() - 1);

        } else {
            //middle of the line, just decrease column
            position.setColumn(position.getColumn() - 1);
        }
    }

    //constructor
    public ProgramTextWrapper(String[] program) {
        this.lines = program;
        ignoreAt = new int[program.length];
        for (int i = 0; i < program.length; i++) {
            String line = program[i];
            boolean inString = false;
            boolean escaped = false;
            boolean lastIsStart = false;
            boolean done = false;

            setPosition(new ProgramPosition(0, 0));

            for (int x = 0; x < line.length(); x++) {
                if (!done) {
                    char c = line.charAt(x);
                    switch (c) {
                        case '/':
                            if (!inString) {
                                //not in String
                                if (lastIsStart) {
                                    //complete line escape
                                    done = true;
                                    ignoreAt[i] = x - 1;
                                } else {
                                    //could be start
                                    lastIsStart = true;
                                }

                            }
                            break;
                        case '"':
                            if (inString) {
                                if (escaped) {
                                    //it is escaped, it's not the end of the String
                                    escaped = false;
                                } else {
                                    //it's not escaped, it's the end of the String
                                    inString = false;
                                }
                            } else {
                                //not in String, so it has to be the start
                                inString = true;
                            }
                            break;
                        case '\\':
                            if (inString) {
                                escaped = !escaped;
                            }
                            //else is in reality a problem, but it will throw a CompileException later
                            break;
                        default:
                            escaped = false;
                            break;
                    }
                }

            }
            if (!done) ignoreAt[i] = -1;
        }
    }

    //returns the actual character and moves the position
    //returns a space char if it is something whitespace
    public char getNextChar() throws EndReachedException {
        return getNextChar(true);
    }

    //skips comments
    public char getNextChar(boolean movePosition) throws EndReachedException {
        //throw exception if necessary
        if (position.getLine() >= lines.length) throw new EndReachedException();
        if (position.getColumn() < lines[position.getLine()].length()) {
            char c = lines[position.getLine()].charAt(position.getColumn());
            if (movePosition) {
                //just increase the column
                position.setColumn(position.getColumn() + 1);
                //check if it is the start of a comment
                if (ignoreAt[position.getLine()] == position.getColumn()) {
                    //the rest if this line has to be ignored, so it jumps
                    position.setLine(position.getLine() + 1);
                    position.setColumn(0);
                    jumpIfNecessary();
                }
            }
            //return the character
            if (Character.isWhitespace(c)) return ' ';
            else return c;
        } else {
            if (movePosition) {
                //move to begin of next line
                position.setLine(position.getLine() + 1);
                position.setColumn(0);
                jumpIfNecessary();
            }
            return ' ';
        }
    }

    //helper method to jump to the next line as long as necessary
    private void jumpIfNecessary() {
        //check if there is a program at all
        if (position.getLine() < ignoreAt.length) {
            if (ignoreAt[position.getLine()] == 0) {
                //it should jump
                //move to begin of next line
                position.setLine(position.getLine() + 1);
                position.setColumn(0);
                jumpIfNecessary();
            }
        }
    }


    //returns a string of alphanumeric or underscore with moving position
    public String getNextWord() throws CompileException {
        return getNextWord(true);
    }

    public String getNextWord(boolean movePosition) throws CompileException {
        ProgramPosition pos = position.getClone();
        StringBuilder sb = new StringBuilder();
        boolean foundRealCharacter = false;
        while (!foundRealCharacter) {
            try {
                char c = getNextChar();
                if (c == ' ') {
                    //not found, continue searching
                } else if (Character.isLetterOrDigit(c) || c == '_') {
                    foundRealCharacter = true;
                    sb.append(c);
                } else {
                    //found something different, throw a Exception
                    throw new CompileException("found illegal character : '" + c + '\'', position.getClone());
                }
            } catch (EndReachedException e) {
                return null;
            }
        }
        boolean foundIllegalCharacter = false;
        while (!foundIllegalCharacter) {
            try {
                char c = getNextChar();
                if (Character.isLetterOrDigit(c)) {
                    //append it, continue searching
                    sb.append(c);
                } else {
                    //stop searching
                    foundIllegalCharacter = true;
                }
            } catch (EndReachedException e) {
                //this is ok, just end the loop
                foundIllegalCharacter = true;
            }
        }
        if (!movePosition) setPosition(pos);
        else moveBackward();

        return sb.toString();
    }

    public ProgramPosition getNextPosition(char toFind, boolean movePosition) {
        ProgramPosition pos = position.getClone();
        boolean found = false;
        while (!found) {
            try {
                char c = getNextChar();
                if (c == toFind) {
                    found = true;
                    ProgramPosition foundPos = position.getClone();
                    if (!movePosition) setPosition(pos);
                    return foundPos;
                }
            } catch (EndReachedException e) {
                //nothing was found
                if (!movePosition) setPosition(pos);
                return null;
            }
        }
        //just necessary for the compiler, this should never happen
        return null;
    }

    //gets the next character which is not a whitespace
    public char getNextNonWhitespaceChar() throws EndReachedException {
        return getNextNonWhitespaceChar(true);
    }

    public char getNextNonWhitespaceChar(boolean movePosition) throws EndReachedException {
        ProgramPosition pos = position.getClone();
        while (true) {
            char c = getNextChar();
            if (c != ' ') {
                if (!movePosition) setPosition(pos);
                return c;
            }
        }
    }

    //get the next char of a List
    public char skipUntil(boolean ignoreStrings, char... chars) throws EndReachedException {
        return skipUntil(true, ignoreStrings, chars);
    }

    public char skipUntil(boolean movePosition, boolean ignoreStrings, char... chars) throws EndReachedException {
        ProgramPosition pos = position.getClone();
        boolean inString = false;
        boolean escape = false;
        while (true) {
            char c = getNextChar();
            if (ignoreStrings) {
                if (c == '"') {
                    if (escape) {
                        escape = false;
                    } else {
                        //end reached
                        inString = !inString;
                    }
                } else if (inString && c == '\\') {
                    if (escape) {
                        escape = false;
                    } else {
                        escape = true;
                    }
                } else {
                    if (escape) escape = false;
                    for (int x = 0; x < chars.length; x++) {
                        if (chars[x] == c) {
                            //found the correct char, end loop and return
                            if (!movePosition) setPosition(pos);
                            return c;
                        }
                    }
                }
            } else {
                for (int x = 0; x < chars.length; x++) {
                    if (chars[x] == c) {
                        //found the correct char, end loop and return
                        if (!movePosition) setPosition(pos);
                        return c;
                    }
                }
            }
        }
    }


    public static class EndReachedException extends Exception {
    }
}
