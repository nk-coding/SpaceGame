package com.nkcoding.interpreter.compiler;


import com.nkcoding.interpreter.*;
import com.nkcoding.interpreter.operators.*;

import java.util.Arrays;
import java.util.List;

public class CompilerHelper {
    //array with all the reserved keywords
    private static final String[] keywords = new String[]{"int", "float", "boolean", "void", "if", "else", "string", "for", "while", "return", "null", "true", "false", "list"};

    //checks if a name is already in a list of MethodDefinition
    public static boolean methodDefinitionsContainName(List<MethodDefinition> methodDefinitions, String name) {
        for (MethodDefinition def : methodDefinitions) {
            if (def.getName().equals(name)) return true;
        }
        return false;
    }

    //check if a name is already in a list of TypeNamePair
    public static boolean typeNamePairsContainName(List<TypeNamePair> typeNamePairs, String name) {
        for (TypeNamePair pair : typeNamePairs) {
            if (pair.getName().equals(name)) return true;
        }
        return false;
    }

    //parse a String
    //start sign should already be checked
    public static String parseString(ProgramTextWrapper text) throws CompileException {
        try {
            char c = text.getNextChar();
            if (c != '"') throw new IllegalArgumentException();
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder();
        boolean endReached = false;
        boolean escape = false;
        while (!endReached) {
            try {
                char next = text.getNextChar();
                if (next == '\\') {
                    if (escape) {
                        //escaped, so add it
                        sb.append(next);
                        escape = false;
                    } else {
                        escape = true;
                    }
                } else if (next == '"') {
                    if (escape) {
                        //escaped, so add it
                        sb.append(next);
                        escape = false;
                    } else {
                        //end reached
                        endReached = true;
                    }
                } else {
                    if (escape) throw new CompileException(next + " can not be escaped", text.getPosition());
                    sb.append(next);
                }
            } catch (ProgramTextWrapper.EndReachedException e) {
                throw new CompileException("expeted: \" found: nothing", text.getPosition());
            }
        }
        return sb.toString();
    }

    //parse an int
    //does stop on everything different that a digit except a point (there it throws a WrongTypeException)
    public static int parseInt(ProgramTextWrapper text, boolean secureType) throws CompileException, WrongTypeException {
        StringBuilder sb = new StringBuilder();
        boolean endReached = false;
        while (!endReached) {
            try {
                char c = text.getNextChar();
                if (Character.isDigit(c)) sb.append(c);
                else if (secureType && c == '.') throw new WrongTypeException();
                else {
                    endReached = true;
                    text.moveBackward();
                }
            } catch (ProgramTextWrapper.EndReachedException e) {
                //this is not the problem of this method
                endReached = true;
            }
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            throw new CompileException(e.toString(), text.getPosition());
        }
    }

    //parse a float
    public static float parseFloat(ProgramTextWrapper text) throws CompileException {
        StringBuilder sb = new StringBuilder();
        boolean endReached = false;
        boolean dotFound = false;
        while (!endReached) {
            try {
                char c = text.getNextChar();
                if (Character.isDigit(c)) sb.append(c);
                else if (c == '.' && !dotFound) {
                    dotFound = true;
                    sb.append(c);
                } else {
                    endReached = true;
                    text.moveBackward();
                }
            } catch (ProgramTextWrapper.EndReachedException e) {
                //this is not the problem of this method
                endReached = true;
            }
        }
        try {
            return Float.parseFloat(sb.toString());
        } catch (NumberFormatException e) {
            throw new CompileException(e.toString(), text.getPosition());
        }
    }

    //get the priority of an operation
    public static int getPriority(OperatorType type) {
        switch (type) {
            case Multiply:
            case Divide:
            case Mod:
                return 12;
            case Add:
            case Subtract:
                return 11;
            case Lesser:
            case LesserEquals:
            case Greater:
            case GreaterEquals:
                return 9;
            case Equals:
            case Unequals:
                return 8;
            case And:
                return 4;
            case Or:
                return 3;
            case AddAssign:
            case Assignment:
            case SubtractAssign:
            case MultiplyAssign:
            case DivideAssign:
            case ModAssign:
                return 1;
            default:
                throw new UnsupportedOperationException();
        }
    }

    //create an expression out of two expressions and an operator
    public static Expression useOperator(Expression exp1, Expression exp2, OperatorType op, ProgramPosition pos) throws CompileException {
        //check for void
//        if (exp1.getType().equals(DataType.Void) || exp2.getType().equals(DataType.Void))
//            throw new CompileException("expected: expression found statement", pos);
        if (isAssignmentOperation(op)) {
            //it is an assignment operation, so check if the first expression is a GetValueExpression
            if (!(exp1 instanceof GetValueExpression))
                throw new CompileException("can only assign to a variable", pos);
            GetStackItem getItem = ((GetValueExpression) exp1).getGetStackItem();
            //correct type if possible
            switch (exp1.getType().name) {
                case DataType.INTEGER_KW:
                    if (exp2.getType().equals(DataType.FLOAT)) exp2 = new FloatToIntegerCast(exp2);
                    break;
                case DataType.FLOAT_KW:
                    if (exp2.getType().equals(DataType.INTEGER)) exp2 = new IntegerToFloatCast(exp2);
                    break;
            }
            //check if type is now correct
            if (!exp1.getType().equals(exp2.getType()))
                throw new CompileException(exp1.getType() + "can't be casted implicitly to " + exp2.getType(), pos);
            //type is correct, return the correct expression
            BinaryOperation binaryOperation = null;
            switch (op) {
                case AddAssign:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new AddFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new AddIntegerOperation();
                            break;
                        case DataType.STRING_KW:
                            binaryOperation = new AddStringOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case SubtractAssign:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new SubtractFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new SubtractIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case MultiplyAssign:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new MultiplyFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new MultiplyIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case DivideAssign:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new DivideFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new DivideIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case ModAssign:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new ModFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new ModIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Assignment:
                    AssignmentOperation assignmentOperation = new AssignmentOperation(getItem, exp1.getType());
                    assignmentOperation.setFirstExpression(exp2);
                    return assignmentOperation;

            }
            binaryOperation.setFirstExpression(exp1);
            binaryOperation.setSecondExpression(exp2);
            AssignmentOperation assignOperation = new AssignmentOperation(getItem, binaryOperation.getType(), binaryOperation);
            return assignOperation;
        } else {
            //correct the type if possible
            if (exp1.getType().equals(DataType.INTEGER) && exp2.getType().equals(DataType.FLOAT))
                exp1 = new IntegerToFloatCast(exp1);
            else if (exp1.getType().equals(DataType.FLOAT) && exp2.getType().equals(DataType.INTEGER))
                exp2 = new IntegerToFloatCast(exp2);
            //now check the type
            if (!exp1.getType().equals(exp2.getType()))
                throw new CompileException(exp1.getType() + "can't be castet implicitly to " + exp2.getType(), pos);
            BinaryOperation binaryOperation = null;
            BinaryExpressionBase binExBase;
            switch (op) {
                case Add:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new AddFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new AddIntegerOperation();
                            break;
                        case DataType.STRING_KW:
                            binaryOperation = new AddStringOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Subtract:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new SubtractFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new SubtractIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Multiply:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new MultiplyFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new MultiplyIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Divide:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new DivideFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new DivideIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Mod:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binaryOperation = new ModFloatOperation();
                            break;
                        case DataType.INTEGER_KW:
                            binaryOperation = new ModIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Equals:
                    binExBase = new EqualsOperation();
                    binExBase.setFirstExpression(exp1);
                    binExBase.setSecondExpression(exp2);
                    return (Expression) binExBase;
                case Unequals:
                    binExBase = new UnequalsOperation();
                    binExBase.setFirstExpression(exp1);
                    binExBase.setSecondExpression(exp2);
                    return (Expression) binExBase;
                case Greater:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binExBase = new GreaterFloatOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        case DataType.INTEGER_KW:
                            binExBase = new GreaterIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case GreaterEquals:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binExBase = new GreaterEqualsFloatOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        case DataType.INTEGER_KW:
                            binExBase = new GreaterEqualsIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        case DataType.STRING_KW:
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case Lesser:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binExBase = new LesserFloatOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        case DataType.INTEGER_KW:
                            binExBase = new LesserIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case LesserEquals:
                    switch (exp1.getType().name) {
                        case DataType.FLOAT_KW:
                            binExBase = new LesserEqualsFloatOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        case DataType.INTEGER_KW:
                            binExBase = new LesserEqualsIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression) binExBase;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case Or:
                    switch (exp1.getType().name) {
                        case DataType.BOOLEAN_KW:
                            binaryOperation = new OrOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case And:
                    switch (exp1.getType().name) {
                        case DataType.BOOLEAN_KW:
                            binaryOperation = new AndOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
            }
            binaryOperation.setFirstExpression(exp1);
            binaryOperation.setSecondExpression(exp2);
            return binaryOperation;
        }
    }

    //checks if an OperationType is an assignment
    private static boolean isAssignmentOperation(OperatorType op) {
        return op == OperatorType.AddAssign || op == OperatorType.SubtractAssign ||
                op == OperatorType.MultiplyAssign || op == OperatorType.DivideAssign ||
                op == OperatorType.ModAssign || op == OperatorType.Assignment;
    }

    //checks if a String is a reserved keyword
    public static boolean isReservedKeyword(String str) {
        return Arrays.asList(keywords).contains(str);
    }

    public static class WrongTypeException extends Exception {
    }

}
