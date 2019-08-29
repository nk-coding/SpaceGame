package com.nkcoding.interpreter.compiler;


import com.nkcoding.interpreter.DoubleToIntegerCast;
import com.nkcoding.interpreter.Expression;
import com.nkcoding.interpreter.GetValueExpression;
import com.nkcoding.interpreter.IntegerToDoubleCast;
import com.nkcoding.interpreter.operators.*;

import java.util.Arrays;
import java.util.List;

public class CompilerHelper {
    //array with all the reserved keywords
    private static final String[] keywords = new String[] {"int", "double", "boolean", "void", "if", "else", "for", "while", "return", "null", "true", "false"};

    //checks if a name is already in a list of MethodDefinition
    public static boolean methodDefinitionsContainName(List<MethodDefinition> methodDefinitions, String name) {
        for (MethodDefinition def : methodDefinitions){
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
        }
        catch (ProgramTextWrapper.EndReachedException e) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder();
        boolean endReached = false;
        boolean escape = false;
        while (!endReached) {
            try {
                char next = text.getNextChar();
                if (next == '\\'){
                    if (escape) {
                        //escaped, so add it
                        sb.append(next);
                        escape = false;
                    }
                    else {
                        escape = true;
                    }
                }
                else if (next == '"'){
                    if (escape) {
                        //escaped, so add it
                        sb.append(next);
                        escape = false;
                    }
                    else {
                        //end reached
                        endReached = true;
                    }
                }
                else {
                    if (escape) throw new CompileException(next + " can not be escaped", text.getPosition());
                    sb.append(next);
                }
            }
            catch (ProgramTextWrapper.EndReachedException e) {
                throw new CompileException("expeted: \" found: nothing", text.getPosition());
            }
        }
        return sb.toString();
    }

    //parse an int
    //does stop on everything different that a digit except a point (there it throws a WrongTypeException)
    public static int parseInt(ProgramTextWrapper text) throws CompileException, WrongTypeException {
        StringBuilder sb = new StringBuilder();
        boolean endReached = false;
        while (!endReached) {
            try {
                char c = text.getNextChar();
                if (Character.isDigit(c)) sb.append(c);
                else if (c == '.') throw new WrongTypeException();
                else {
                    endReached = true;
                    text.moveBackward();
                }
            }
            catch (ProgramTextWrapper.EndReachedException e) {
                //this is not the problem of this method
                endReached = true;
            }
        }
        try {
            return Integer.parseInt(sb.toString());
        }
        catch (NumberFormatException e) {
            throw new CompileException(e.toString(), text.getPosition());
        }
    }

    //parse a double
    public static double parseDouble(ProgramTextWrapper text) throws CompileException {
        StringBuilder sb = new StringBuilder();
        boolean endReached = false;
        boolean dotFound = false;
        while (!endReached) {
            try {
                char c = text.getNextChar();
                if (Character.isDigit(c)) sb.append(c);
                else if (c == '.' && !dotFound){
                    dotFound = true;
                    sb.append(c);
                }
                else{
                    endReached = true;
                    text.moveBackward();
                }
            }
            catch (ProgramTextWrapper.EndReachedException e) {
                //this is not the problem of this method
                endReached = true;
            }
        }
        try {
            return Double.parseDouble(sb.toString());
        }
        catch (NumberFormatException e) {
            throw new CompileException(e.toString(), text.getPosition());
        }
    }

    //get the priority of an operation
    public static int getPriority(OperatorType type) {
        switch(type) {
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
        if (exp1.getType().equals(DataTypes.Void) || exp2.getType().equals(DataTypes.Void))
            throw new CompileException("expected: expression found statement", pos);
        if (isAssignmentOperation(op)) {
            //it is an assignment operation, so check if the first expression is a GetValueExpression
            if (!(exp1 instanceof GetValueExpression))
                throw new CompileException("can only assign to a variable", pos);
            String variableName = ((GetValueExpression)exp1).getName();
            //correct type if possible
            switch (exp1.getType()) {
                case DataTypes.Integer:
                    if (exp2.getType().equals(DataTypes.Double)) exp2 = new DoubleToIntegerCast(exp2);
                    break;
                case DataTypes.Double:
                    if (exp2.getType().equals(DataTypes.Integer)) exp2 = new IntegerToDoubleCast(exp2);
                    break;
            }
            //check if type is now correct
            if (!exp1.getType().equals(exp2.getType()))
                throw new CompileException(exp1.getType() + "can't be casted implicitly to " + exp2.getType(), pos);
            //type is correct, return the correct expression
            BinaryOperation binaryOperation = null;
            switch (op) {
                case AddAssign:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new AddAssignmentDoubleOperation(variableName);
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new AddAssignmentIntegerOperation(variableName);
                            break;
                        case DataTypes.String:
                            binaryOperation = new AddAssignmentStringOperation(variableName);
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case SubtractAssign:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new SubtractAssignmentDoubleOperation(variableName);
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new SubtractAssignmentIntegerOperation(variableName);
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case MultiplyAssign:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new MultiplyAssignmentDoubleOperation(variableName);
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new MultiplyAssignmentIntegerOperation(variableName);
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case DivideAssign:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new DivideAssignmentDoubleOperation(variableName);
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new DivideAssignmentIntegerOperation(variableName);
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case ModAssign:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new ModAssignmentDoubleOperation(variableName);
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new ModAssignmentIntegerOperation(variableName);
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Assignment:
                    AssignmentOperation assignmentOperation = new AssignmentOperation(variableName, exp1.getType());
                    assignmentOperation.setFirstExpression(exp2);
                    return assignmentOperation;

            }
            binaryOperation.setFirstExpression(exp1);
            binaryOperation.setSecondExpression(exp2);
            return binaryOperation;
        }
        else {
            //correct the type if possible
            if (exp1.getType().equals(DataTypes.Integer) && exp2.getType().equals(DataTypes.Double))
                exp1 = new IntegerToDoubleCast(exp1);
            else if (exp1.getType().equals(DataTypes.Double) && exp2.getType().equals(DataTypes.Integer))
                exp2 = new IntegerToDoubleCast(exp2);
            //now check the type
            if (!exp1.getType().equals(exp2.getType()))
                throw new CompileException(exp1.getType() + "can't be castet implicitly to " + exp2.getType(), pos);
            BinaryOperation binaryOperation = null;
            BinaryExpressionBase binExBase;
            switch(op){
                case Add:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new AddDoubleOperation();
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new AddIntegerOperation();
                            break;
                        case DataTypes.String:
                            binaryOperation = new AddStringOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Subtract:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new SubtractDoubleOperation();
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new SubtractIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Multiply:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new MultiplyDoubleOperation();
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new MultiplyIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Divide:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new DivideDoubleOperation();
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new DivideIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Mod:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binaryOperation = new ModDoubleOperation();
                            break;
                        case DataTypes.Integer:
                            binaryOperation = new ModIntegerOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case Equals:
                    binExBase =  new EqualsOperation();
                    binExBase.setFirstExpression(exp1);
                    binExBase.setSecondExpression(exp2);
                    return (Expression)binExBase;
                case Unequals:
                    binExBase =  new UnequalsOperation();
                    binExBase.setFirstExpression(exp1);
                    binExBase.setSecondExpression(exp2);
                    return (Expression)binExBase;
                case Greater:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binExBase =  new GreaterDoubleOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        case DataTypes.Integer:
                            binExBase =  new GreaterIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case GreaterEquals:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binExBase =  new GreaterEqualsDoubleOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        case DataTypes.Integer:
                            binExBase =  new GreaterEqualsIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        case DataTypes.String:
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case Lesser:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binExBase =  new LesserDoubleOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        case DataTypes.Integer:
                            binExBase =  new LesserIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case LesserEquals:
                    switch (exp1.getType()) {
                        case DataTypes.Double:
                            binExBase =  new LesserEqualsDoubleOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        case DataTypes.Integer:
                            binExBase =  new LesserEqualsIntegerOperation();
                            binExBase.setFirstExpression(exp1);
                            binExBase.setSecondExpression(exp2);
                            return (Expression)binExBase;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                case Or:
                    switch (exp1.getType()) {
                        case DataTypes.Boolean:
                            binaryOperation = new OrOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
                case And:
                    switch (exp1.getType()) {
                        case DataTypes.Boolean:
                            binaryOperation = new AndOperation();
                            break;
                        default:
                            throw new CompileException("you can't use this operator with a " + exp1.getType(), pos);
                    }
                    break;
            }
            binaryOperation.setFirstExpression(exp1);
            binaryOperation.setSecondExpression(exp2);
            return  binaryOperation;
        }
    }

    //checks if an OperationType is an assignment
    private static boolean isAssignmentOperation(OperatorType op) {
        return op == OperatorType.AddAssign || op == OperatorType.SubtractAssign ||
                op == OperatorType.MultiplyAssign || op == OperatorType.DivideAssign ||
                op == OperatorType.ModAssign || op == OperatorType.Assignment;
    }

    //checks if a String is a reserved keyword
    public static boolean isReservedKeyword(String str){
        return Arrays.asList(keywords).contains(str);
    }

    public static class WrongTypeException extends Exception {}

}
