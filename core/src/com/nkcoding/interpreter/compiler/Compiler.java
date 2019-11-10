package com.nkcoding.interpreter.compiler;

import com.nkcoding.interpreter.*;
import com.nkcoding.interpreter.operators.*;
import com.nkcoding.spacegame.spaceship.ComponentType;
import com.nkcoding.spacegame.spaceship.ExternalPropertyData;
import com.nkcoding.spacegame.spaceship.ShipDef;
import com.nkcoding.util.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Compiler {
    //the ProgramTextWrapper that is normally used to handle the text
    private ProgramTextWrapper text;

    //the Methods object which manages extern and predefined methods
    private Methods methods;

    private CompilerStack stack;

    //the MethodDefinition of the actual method
    //used to create return types
    private MethodDefinition actualMethod;

    //constructor
    public Compiler(String[] lines, MethodDefinition[] externMethods) {
        text = new ProgramTextWrapper(lines);
        methods = new Methods();
        methods.addExternMethods(externMethods);
        stack = new CompilerStack();
    }

    //constructor with default methods
    public Compiler(String text, ShipDef def) {
        //create the external method statements for the components
        HashMap<String, ExternalPropertyData> externalPropertyDatas = new HashMap<>();
        for (ComponentType com : ComponentType.values()) {
            for (ExternalPropertyData data : com.propertyDefs) {
                if (!externalPropertyDatas.containsKey(data.name)) {
                    externalPropertyDatas.put(data.name, data);
                }
            }
        }
        //add from Ship
        for (ExternalPropertyData data : def.properties.values()) {
            if (!externalPropertyDatas.containsKey(data.name)) {
                externalPropertyDatas.put(data.name, data);
            }
        }
        ArrayList<MethodDefinition> methodDefinitions = new ArrayList<>();
        for (ExternalPropertyData data : externalPropertyDatas.values()) {
            data.addExternalMethodDefs(methodDefinitions);
        }
        String lines[] = text.split("\\r?\\n");

        this.text = new ProgramTextWrapper(lines);
        methods = new Methods();
        methods.addExternMethods(methodDefinitions.toArray(MethodDefinition[]::new));
        stack = new CompilerStack();
    }

    //updates the compiler
    public void update(String[] lines) {
        stack = new CompilerStack();
        text = new ProgramTextWrapper(lines);
        actualMethod = null;
        methods.setNormalMethods(new MethodStatement[0]);
    }

    //compile the stuff
    public Program compile() throws CompileException {
        //get the Definitions for all methods in the program
        Tuple<List<MethodDefinition>, ConcurrentHashMap<String, ConcurrentStackItem>> pair = createMethodDefinitions();
        //pair.v2.values().forEach(System.out::println);
        List<MethodDefinition> methodDefinitions = pair.v1;
        //get the globalVariables
        //set the text position back to the start
        text.setPosition(new ProgramPosition(0, 0));
        //create the methodStatements that will be returned later
        MethodStatement[] normalMethods = new MethodStatement[methodDefinitions.size()];
        for (int x = 0; x < methodDefinitions.size(); x++) {
            //create the corrosponding method
            normalMethods[x] = new MethodStatement(methodDefinitions.get(x));
        }
        methods.setNormalMethods(normalMethods);
        //I can't believe I reached this point
        boolean completed = false;
        for (int x = 0; x < normalMethods.length; x++) {
            try {
                text.skipUntil(true, '{');
                text.moveBackward();
            } catch (ProgramTextWrapper.EndReachedException e) {
                throw new IllegalStateException("ERROR: please check the compiler :/");
            }
            //start stack level
            stack.beginStackLevel();
            //add all the parameters to the stack
            TypeNamePair[] parameters = normalMethods[x].getDefinition().getParameters();
            //add all parameters to stack
            for (TypeNamePair parameter : parameters) {
                stack.addToStack(parameter.getName(), parameter.getType());
            }
            //set the actualMethod
            actualMethod = normalMethods[x].getDefinition();
            //get the method main body
            normalMethods[x].setStatements(compileStatementBlock());
            //end stack level
            stack.clearStackLevel();
        }
        return new Program(normalMethods, pair.v2);
    }

    //create the MethodDefinitions for a complete program
    private Tuple<List<MethodDefinition>, ConcurrentHashMap<String, ConcurrentStackItem>> createMethodDefinitions() throws CompileException {
        //begin a stack level for global variables
        stack.beginStackLevel();
        //list where all the definitions are stored in
        ArrayList<MethodDefinition> definitions = new ArrayList<>();

        boolean endReached = false;

        while (!endReached) {
            //return type expected
            String returnType = text.getNextWord();
            if (returnType == null) {
                //nothing found, this is actually ok
                endReached = true;
            } else {
                //here it really begins
                //if it does not contain the return type, something must be wrong
                if (!DataType.contains(returnType))
                    throw new CompileException("Expected: return type, found : " + returnType, text.getPosition().getClone());
                //everything ok
                //create a new methodDefinition
                NormalMethodDefinition def = new NormalMethodDefinition(text.getPosition().getLine());
                def.setReturnType(DataType.fromName(returnType));
                //the method name
                def.setName(text.getNextWord());
                //try get the bracket, if that is not possible throw an exception
                try {
                    char beginChar = text.getNextNonWhitespaceChar(true);
                    if (beginChar == '(') {
                        //check if the Name is already in use (support for overloading will be added later)
                        if (CompilerHelper.methodDefinitionsContainName(definitions, def.getName()) || methods.methodExists(def.getName()) != null)
                            throw new CompileException("a method with the name " + def.getName() + " already exists", text.getPosition());
                        //check if name is keyword
                        if (CompilerHelper.isReservedKeyword(def.getName()))
                            throw new CompileException("reserved keyword", text.getPosition());
                        //add it to the list
                        definitions.add(def);

                        //check for Arguments
                        boolean allArgumentsFound = false;
                        ArrayList<TypeNamePair> pairs = new ArrayList<>();
                        while (!allArgumentsFound) {
                            try {
                                char possibleEndBracket = text.getNextNonWhitespaceChar();
                                if (possibleEndBracket == ')') {
                                    //the end was found
                                    //no more parameters
                                    allArgumentsFound = true;
                                } else {
                                    //correct position
                                    text.moveBackward();
                                    //there is (or at least should be) a parameter
                                    String parameterType = text.getNextWord();
                                    //check if the type is correct
                                    if (!DataType.containsDataType(parameterType))
                                        throw new CompileException(parameterType + " is no correct DataType", text.getPosition());
                                    //correct type was found, get name
                                    TypeNamePair pair = new TypeNamePair(text.getNextWord(), DataType.fromName(parameterType));
                                    //check if parameter name is already in use
                                    if (CompilerHelper.typeNamePairsContainName(pairs, pair.getName()))
                                        throw new CompileException("a parameter with the name " + pair.getName() + " already exists", text.getPosition());
                                    //check if parameter name is reserved keyword
                                    if (CompilerHelper.isReservedKeyword(pair.getName()))
                                        throw new CompileException("reserved keyword", text.getPosition());
                                    //add pair
                                    pairs.add(pair);
                                    //check if next char is a comma or and end bracket
                                    //then everything is ok (the bracket will be recognized in the next loop, otherwise throw an Exception
                                    char possibleComma = text.getNextNonWhitespaceChar(true);
                                    if (possibleComma == ')')
                                        allArgumentsFound = true;
                                    else if (possibleComma != ',')
                                        throw new CompileException("expected: ) found " + possibleComma, text.getPosition());
                                }
                            } catch (ProgramTextWrapper.EndReachedException e) {
                                //no end bracket was found
                                throw new CompileException("expected: ) found nothing", text.getPosition());
                            }
                        }
                        //add the parameters
                        def.setParameters(pairs.toArray(TypeNamePair[]::new));
                        //skip the internal part of the method
                        //this part will be checked later by the compiler, so do not waste to much effort here

                        //shows how deep into brackets the program is at a specific time
                        int bracketLevel = 0;
                        //the next non whitespace char should be a opening bracket, otherwise the method has no body so throw an exception
                        try {
                            char methodBodyOpen = text.getNextNonWhitespaceChar();
                            if (methodBodyOpen == '{') {
                                //method found as expected
                                bracketLevel++;
                            } else {
                                throw new CompileException("expected: { found: " + methodBodyOpen, text.getPosition());
                            }
                        } catch (ProgramTextWrapper.EndReachedException e) {
                            //no body
                            throw new CompileException("expected: { found: nothing", text.getPosition());
                        }
                        //continue until the end of the body is reached
                        while (bracketLevel > 0) {
                            try {
                                char nextBracket = text.skipUntil(true, '{', '}');
                                if (nextBracket == '{') bracketLevel++;
                                else bracketLevel--;
                            } catch (ProgramTextWrapper.EndReachedException e) {
                                throw new CompileException("expected: } found: nothing", text.getPosition());
                            }
                        }
                    } else if (beginChar == ';') {
                        if (stack.exists(def.getName())) {
                            throw new CompileException("a global variable with the name " + def.getName() + " already exists", text.getPosition());
                        } else if (CompilerHelper.isReservedKeyword(def.getName())) {
                            throw new CompileException("expected: variable name found: reserved keyword", text.getPosition());
                        }
                        //add global variable
                        stack.addToStack(def.getName(), def.getReturnType());
                    } else {
                        throw new CompileException("expected: ( or ; found: " + beginChar, text.getPosition());
                    }
                } catch (ProgramTextWrapper.EndReachedException e) {
                    //begin bracket not found
                    throw new CompileException("expected: ( found: nothing", text.getPosition());
                }
            }
        }
        return new Tuple<>(definitions, stack.getGlobalVariables());
    }

    //compiles a complete Expression
    //after return, text is at the position that make it stop
    //stop chars are: ;,{} and everything different that is illegal (e.g. %, &, )...)
    private Expression compileCompleteExpression() throws CompileException {
        ArrayList<Expression> expressions = new ArrayList<>();
        ArrayList<Operation> operations = new ArrayList<>();
        //the main loop to get the complete expression
        boolean gotAllExpressions = false;
        while (!gotAllExpressions) {
            //get next expression
            expressions.add(compileSingleExpression());

            //try get the operator
            try {
                //System.out.println("nextchar: " + text.getNextChar(false));
                char op1 = text.getNextNonWhitespaceChar();
                //System.out.println("try get op1: " + op1);
                if (op1 == '+' || op1 == '-' || op1 == '*' || op1 == '/' || op1 == '%' || op1 == '<' || op1 == '>' || op1 == '=') {
                    //System.out.println("op1 is in list");
                    try {
                        char op2 = text.getNextChar();
                        if (op2 == '=') {
                            switch (op1) {
                                case '+':
                                    operations.add(new Operation(OperatorType.AddAssign, expressions.size() - 1, expressions.size()));
                                    break;
                                case '-':
                                    operations.add(new Operation(OperatorType.SubtractAssign, expressions.size() - 1, expressions.size()));
                                    break;
                                case '*':
                                    operations.add(new Operation(OperatorType.MultiplyAssign, expressions.size() - 1, expressions.size()));
                                    break;
                                case '/':
                                    operations.add(new Operation(OperatorType.DivideAssign, expressions.size() - 1, expressions.size()));
                                    break;
                                case '%':
                                    operations.add(new Operation(OperatorType.ModAssign, expressions.size() - 1, expressions.size()));
                                    break;
                                case '<':
                                    operations.add(new Operation(OperatorType.LesserEquals, expressions.size() - 1, expressions.size()));
                                    break;
                                case '>':
                                    operations.add(new Operation(OperatorType.GreaterEquals, expressions.size() - 1, expressions.size()));
                                    break;
                                case '=':
                                    operations.add(new Operation(OperatorType.Equals, expressions.size() - 1, expressions.size()));
                            }
                        } else {
                            text.moveBackward();
                            switch (op1) {
                                case '+':
                                    operations.add(new Operation(OperatorType.Add, expressions.size() - 1, expressions.size()));
                                    break;
                                case '-':
                                    operations.add(new Operation(OperatorType.Subtract, expressions.size() - 1, expressions.size()));
                                    break;
                                case '*':
                                    operations.add(new Operation(OperatorType.Multiply, expressions.size() - 1, expressions.size()));
                                    break;
                                case '/':
                                    operations.add(new Operation(OperatorType.Divide, expressions.size() - 1, expressions.size()));
                                    break;
                                case '%':
                                    operations.add(new Operation(OperatorType.Mod, expressions.size() - 1, expressions.size()));
                                    break;
                                case '<':
                                    operations.add(new Operation(OperatorType.Lesser, expressions.size() - 1, expressions.size()));
                                    break;
                                case '>':
                                    operations.add(new Operation(OperatorType.Greater, expressions.size() - 1, expressions.size()));
                                    break;
                                case '=':
                                    operations.add(new Operation(OperatorType.Assignment, expressions.size() - 1, expressions.size()));
                            }
                        }
                    } catch (ProgramTextWrapper.EndReachedException e) {
                        throw new CompileException("expected: expression found: nothing", text.getPosition());
                    }
                } else if (op1 == '&' || op1 == '|') {
                    try {
                        char op2 = text.getNextChar();
                        if (op2 == op1) {
                            switch (op1) {
                                case '&':
                                    operations.add(new Operation(OperatorType.And, expressions.size() - 1, expressions.size()));
                                    break;
                                case '|':
                                    operations.add(new Operation(OperatorType.Or, expressions.size() - 1, expressions.size()));
                                    break;
                            }
                        } else throw new CompileException("expected: " + op1 + " found: " + op2, text.getPosition());

                    } catch (ProgramTextWrapper.EndReachedException e) {
                        throw new CompileException("expected: " + op1 + " found: nothing", text.getPosition());
                    }
                } else {
                    //no operator found
                    gotAllExpressions = true;
                    text.moveBackward();
                }
            } catch (ProgramTextWrapper.EndReachedException e) {
                //this is not the problem here
                gotAllExpressions = true;
            }
        }
        //got all expressions
        //the funny part starts now
        //use the expressions to return one expression
        //return the expression if there is just one
        if (expressions.size() == 1) return expressions.get(0);
        //query the operations until all are finished
        Collections.sort(operations);
        Collections.reverse(operations);
        for (Operation op : operations) {
            Expression oldExp1 = expressions.get(op.getExp1());
            Expression oldExp2 = expressions.get(op.getExp2());
            Expression newExp = CompilerHelper.useOperator(oldExp1, oldExp2, op.getType(), text.getPosition()); //the position is temporary
            for (int x = 0; x < expressions.size(); x++) {
                if (expressions.get(x) == oldExp1 || expressions.get(x) == oldExp2) expressions.set(x, newExp);
            }
        }
        return expressions.get(0);
    }

    //compile a single expression, also handle list stuff
    private Expression compileSingleExpression() throws CompileException{
        try {
            //get the expression
            char possibleListChar = text.getNextNonWhitespaceChar();
            Expression exp = null;
            if (possibleListChar == '[') {
                //it's a list, yay
                ArrayList<Expression> listInitExpressions = new ArrayList<>();
                boolean completedList = false;
                //get all init expressions
                while (!completedList) {
                    //get the expression
                    listInitExpressions.add(compileCompleteExpression());
                    //check for next or ending
                    try {
                        char nextOrEnding = text.getNextNonWhitespaceChar();
                        if (nextOrEnding == ',') {
                            //just continue searching
                        } else if (nextOrEnding == ']') {
                            completedList = true;
                        } else {
                            throw new CompileException("expected: , or ] found: " + nextOrEnding, text.getPosition());
                        }
                    } catch (ProgramTextWrapper.EndReachedException e) {
                        throw new CompileException("expected: , or ] found: nothing", text.getPosition());
                    }
                }
                exp = new CreateListExpression(listInitExpressions.toArray(Expression[]::new));
            } else {
                //just a normal expression
                //reset the position
                text.moveBackward();
                exp = compileInternalExpression();
            }
            if (exp.getType().name.equals(DataType.LIST_KW)) {
                return compileListExpression(exp);
            }
            else {
                return exp;
            }
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: expression, found: nothing", text.getPosition());
        }
    }

    //check for getter
    private Expression compileListExpression(Expression expression) throws CompileException {
        //check for dots
        try {
            char possibleDot = text.getNextNonWhitespaceChar();
            if (possibleDot == '.') {
                try {
                    char possibleIndex = text.getNextNonWhitespaceChar(false);
                    if (Character.isDigit(possibleIndex)) {
                        //get an integer
                        try {
                            int index = CompilerHelper.parseInt(text, false);
                            if (index >= expression.getType().listTypes.length) {
                                throw new CompileException("index " + index + " is out of bounds", text.getPosition());
                            }
                            GetterExpression getter = new GetterExpression(expression, index);
                            if (getter.getType().name.equals(DataType.LIST_KW)) {
                                return compileListExpression(getter);
                            } else {
                                return getter;
                            }
                        } catch (CompilerHelper.WrongTypeException e) {
                            throw new CompileException("expected: index", text.getPosition());
                        }
                    } else {
                        String identifier = text.getNextWord();
                        TypeNamePair[] possibleIdents = expression.getType().listTypes;
                        int index = -1;
                        for (int x = 0; x < possibleIdents.length; x++) {
                            if (index < 0 && possibleIdents[x].getName().equals(identifier)) {
                                index = x;
                            }
                        }
                        if (index < 0) {
                            throw new CompileException("cannot find identifier: " + identifier, text.getPosition());
                        } else {
                            GetterExpression getter = new GetterExpression(expression, index);
                            if (getter.getType().name.equals(DataType.LIST_KW)) {
                                return compileListExpression(getter);
                            } else {
                                return getter;
                            }
                        }
                    }
                } catch (ProgramTextWrapper.EndReachedException e) {
                    throw new CompileException("expected: identifier or index, found: nothing", text.getPosition());
                }
            } else {
                text.moveBackward();
                return expression;
            }
        } catch (ProgramTextWrapper.EndReachedException e) {
            //do nothing, this is not the problem of this method
            return expression;
        }
    }


    //compiles a single internal expression (does NOT handle list features)
    private Expression compileInternalExpression() throws CompileException {
        //check if there is an unary minus
        boolean unaryMinus = false;
        boolean negateBoolean = false;
        try {
            char possibleSpecialChar = text.getNextNonWhitespaceChar();
            if (possibleSpecialChar == '-') unaryMinus = true;
            else if (possibleSpecialChar == '!') negateBoolean = true;
            else text.moveBackward(); //did not find an unaryMinus, so it's now the problem of the
        } catch (ProgramTextWrapper.EndReachedException e) {
            //did not find an unary minus, but did not find anything else XD
            throw new CompileException("expected: expression, found: nothing", text.getPosition());
        }
        //compile the main expression
        Expression mainExpression = null;
        //get the first character to check if it is a raw value expression
        char firstOfMain;
        try {
            firstOfMain = text.getNextNonWhitespaceChar();
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: expression, found: nothing", text.getPosition());
        }

        //check for bracket region
        if (firstOfMain == '(') {
            //bracket region, compileCompleteExpression
            mainExpression = compileCompleteExpression();
            //check for end bracket
            try {
                char possibleEndBracket = text.getNextNonWhitespaceChar();
                if (possibleEndBracket != ')')
                    throw new CompileException("expected: ) found: " + possibleEndBracket, text.getPosition());
            } catch (ProgramTextWrapper.EndReachedException e) {
                throw new CompileException("expected: ) found: nothing", text.getPosition());
            }
        } else {
            //correct position
            text.moveBackward();
            if (firstOfMain == '"') {
                //it is a raw String
                mainExpression = new RawValueExpression<>(CompilerHelper.parseString(text), DataType.STRING);
            } else if (firstOfMain == '.') {
                //this is a legal float
                mainExpression = new RawValueExpression<>(CompilerHelper.parseFloat(text), DataType.FLOAT);
            } else if (Character.isDigit(firstOfMain)) {
                //it is a raw float or int, find it out with the exception
                ProgramPosition pos = text.getPosition();
                try {
                    int val = CompilerHelper.parseInt(text, true);
                    mainExpression = new RawValueExpression<>(val, DataType.INTEGER);
                } catch (CompilerHelper.WrongTypeException e) {
                    //it was a float
                    //reset position and try again
                    text.setPosition(pos);
                    mainExpression = new RawValueExpression<>(CompilerHelper.parseFloat(text), DataType.FLOAT);
                }
            } else {
                //it's not a float or an integer, so get the complete String
                String exp = text.getNextWord();
                //check if it is a raw boolean
                if (exp.equals("true") || exp.equals("false")) {
                    //it is a raw boolean
                    mainExpression = new RawValueExpression<>(exp.equals("true"), DataType.BOOLEAN);
                } else {
                    //check if it is a method or not
                    boolean isMethod = false;
                    try {
                        char next = text.getNextNonWhitespaceChar();
                        if (next == '(') {
                            isMethod = true;
                        } else {
                            text.moveBackward();
                        }
                    } catch (ProgramTextWrapper.EndReachedException e) {
                        //in reality, this should never happen
                        //but this is not the problem of this method at this time
                    }
                    if (!isMethod) {
                        //it must be a variable
                        if (stack.exists(exp)) {
                            //the variable exists, everything is ok
                            mainExpression = new GetValueExpression(exp, stack.getType(exp));
                        } else throw new CompileException(exp + " is no known variable or type", text.getPosition());
                    } else {
                        //it is a method
                        mainExpression = compileMethod(exp);
                    }
                }
            }
        }


        //check for postfix
        //get the position manually because the moveBackward could cause trouble if there was an endReachedException
        ProgramPosition pos = text.getPosition();
        boolean plusPostfix = false;
        boolean minusPostfix = false;
        try {
            char possiblePostfixStart = text.getNextNonWhitespaceChar();
            if (possiblePostfixStart == '+' || possiblePostfixStart == '-') {
                //the start is correct, now continue searching
                if (text.getNextChar() == possiblePostfixStart) {
                    //found a postfix
                    //check the type
                    if (possiblePostfixStart == '+') plusPostfix = true;
                    else minusPostfix = true;
                } else {
                    //correct position
                    text.setPosition(pos);
                }
            } else {
                text.setPosition(pos);
            }
        } catch (ProgramTextWrapper.EndReachedException e) {
            //obviously, there is no postfix
            //correct the position
            text.setPosition(pos);
        }
        //apply postfix if necessary
        if (plusPostfix || minusPostfix) {
            //check if the main expression is a variable
            if (!(mainExpression instanceof GetValueExpression))
                throw new CompileException("a postfix operator can only be used with a variable", text.getPosition());
            GetValueExpression getValueExpression = (GetValueExpression) mainExpression;
            //check if mainExpression is of correct type
            switch (mainExpression.getType().name) {
                case DataType.INTEGER_KW:
                    AddAssignmentIntegerOperation aaio = new AddAssignmentIntegerOperation(getValueExpression.getName());
                    aaio.setFirstExpression((Expression<Integer>) mainExpression);
                    aaio.setSecondExpression(new RawValueExpression<Integer>(plusPostfix ? 1 : -1, DataType.INTEGER));
                    mainExpression = aaio;
                    break;
                case DataType.FLOAT_KW:
                    AddAssignmentFloatOperation aado = new AddAssignmentFloatOperation(getValueExpression.getName());
                    aado.setFirstExpression((Expression<Float>) mainExpression);
                    aado.setSecondExpression(new RawValueExpression<Float>(plusPostfix ? 1f : -1f, DataType.FLOAT));
                    mainExpression = aado;
                    break;
                default:
                    throw new CompileException("type mismatch: expected: int or float found: " + mainExpression.getType(), text.getPosition());
            }
        }
        //apply unary minus if necessary
        if (unaryMinus) {
            //check if the main Expression is of correct type
            switch (mainExpression.getType().name) {
                case DataType.INTEGER_KW:
                    NegateIntegerOperation nio = new NegateIntegerOperation();
                    nio.setFirstExpression((Expression<Integer>) mainExpression);
                    mainExpression = nio;
                    break;
                case DataType.FLOAT_KW:
                    NegateFloatOperation ndo = new NegateFloatOperation();
                    ndo.setFirstExpression((Expression<Float>) mainExpression);
                    mainExpression = ndo;
                    break;
                default:
                    throw new CompileException("unary minus can only be applied to int or float expressions", text.getPosition());

            }
        }
        //apply negateBoolean if necessary
        if (negateBoolean) {
            //check if the main Expression is of correct type
            if (mainExpression.getType().equals(DataType.BOOLEAN)) {
                NegateBooleanOperation nbo = new NegateBooleanOperation();
                nbo.setFirstExpression((Expression<Boolean>) mainExpression);
                mainExpression = nbo;
            } else throw new CompileException("only a boolean expression can be negated", text.getPosition());
        }
        return mainExpression;
    }

    //compile a method
    //the text position is after the begin bracket
    //after compile, text position is after the end bracket
    private Expression compileMethod(String methodName) throws CompileException {
        //get the method definition
        MethodType type = null;
        //get the MethodDefinition if possible
        MethodDefinition def = methods.methodExists(methodName);
        //check if the method exists, else throw an exception
        if (def != null) {
            //method exists
            //parse all arguments
            ArrayList<Expression> methodArguments = new ArrayList<>();
            //check if parameter list already ends
            char possibleEnd;
            try {
                possibleEnd = text.getNextNonWhitespaceChar();
            } catch (ProgramTextWrapper.EndReachedException e) {
                throw new CompileException("expected: ) found: nothing", text.getPosition());
            }
            if (possibleEnd != ')') {
                text.moveBackward();
                //get all other arguments
                boolean allArgumentsFound = false;
                while (!allArgumentsFound) {
                    //compile the next expression
                    methodArguments.add(compileCompleteExpression());
                    //check if there are other arguments
                    try {
                        possibleEnd = text.getNextNonWhitespaceChar();
                    } catch (ProgramTextWrapper.EndReachedException e) {
                        throw new CompileException("expected: ) found: nothing", text.getPosition());
                    }
                    switch (possibleEnd) {
                        case ')':
                            //the arguments list is over
                            allArgumentsFound = true;
                            break;
                        case ',':
                            //there are move arguments
                            break;
                        default:
                            throw new CompileException("found: " + possibleEnd + " expected: )", text.getPosition());
                    }
                }
            }
            //check if the signature fits or not
            TypeNamePair[] parameters = def.getParameters();
            if (methodArguments.size() != parameters.length)
                throw new CompileException("wrong number of arguments: expected: " + parameters.length + " found: " + methodArguments.size(), text.getPosition());
            //amount fits, now check the types and correct it if possible
            Expression[] arguments = new Expression[parameters.length];
            for (int x = 0; x < parameters.length; x++) {
                switch (methodArguments.get(x).getType().name) {
                    case DataType.INTEGER_KW:
                        switch (parameters[x].getType().name) {
                            case DataType.INTEGER_KW:
                                //same type, everything ok
                                arguments[x] = methodArguments.get(x);
                                break;
                            case DataType.FLOAT_KW:
                                //cast implicitly
                                arguments[x] = new IntegerToFloatCast((Expression<Integer>) methodArguments.get(x));
                                break;
                            default:
                                //type does not fit, throw an exception
                                throw new CompileException("type mismatch: int can not be casted implicitly to " + parameters[x].getType(), text.getPosition());
                        }
                        break;
                    case DataType.FLOAT_KW:
                        switch (parameters[x].getType().name) {
                            case DataType.FLOAT_KW:
                                //same type, everything ok
                                arguments[x] = methodArguments.get(x);
                                break;
                            case DataType.INTEGER_KW:
                                //cast implicitly
                                arguments[x] = new FloatToIntegerCast((Expression<Float>) methodArguments.get(x));
                                break;
                            default:
                                //type does not fit, throw an exception
                                throw new CompileException("type mismatch: float can not be casted implicitly to " + parameters[x].getType(), text.getPosition());
                        }
                        break;
                    default:
                        if (methodArguments.get(x).getType().equals(parameters[x].getType())) {
                            //same type, ok
                            arguments[x] = methodArguments.get(x);
                        } else {
                            //type does not fit, throw an exception
                            throw new CompileException("type mismatch: " + methodArguments.get(x).getType() + " can not be casted implicitly to " + parameters[x].getType(), text.getPosition());
                        }
                }
            }

            switch (def.getMethodType()) {
                case Normal:
                    MethodWrapperStatement methWrapper = new MethodWrapperStatement(def.getReturnType());
                    methWrapper.setName(def.getName());
                    methWrapper.setMethodStatement(methods.getNormalMethod(def));
                    Statement[] statements = new Statement[arguments.length];
                    for (int x = 0; x < arguments.length; x++) {
                        //crate set value statements
                        DefineValueStatement defineValueStatement = new DefineValueStatement(parameters[x].getType());
                        defineValueStatement.setName(parameters[x].getName());
                        defineValueStatement.setValueExpression(arguments[x]);
                        statements[x] = defineValueStatement;
                    }
                    methWrapper.setInitStatements(statements);
                    return methWrapper;
                case Predefined:
                    PredefinedMethodStatement preDef = new PredefinedMethodStatement(def.getReturnType());
                    preDef.setName(def.getName());
                    preDef.setParameterExpressions(arguments);
                    preDef.setPredefinedMethod(methods.getPredefinedMethod(def));
                    return preDef;
                case External:
                    ExternalMethodStatement exMeth = new ExternalMethodStatement(def.getReturnType());
                    exMeth.setName(def.getName());
                    exMeth.setParameterExpressions(arguments);
                    return exMeth;
                default:
                    return null;
            }
        } else throw new CompileException("method " + methodName + " does not exist", text.getPosition());
    }

    //compiles a statement block
    //if it starts with {, it compiles until }, otherwise it just compiles a singe statement
    //this always starts end ends a stack level
    private Statement[] compileStatementBlock() throws CompileException {
        //begin the stack level
        stack.beginStackLevel();
        boolean allStatementsFound = false;
        //find out if it is a single statement
        boolean singleStatement = false;
        try {
            char possibleBracket = text.getNextNonWhitespaceChar();
            if (possibleBracket != '{') {
                singleStatement = true;
                text.moveBackward();
            }
        } catch (ProgramTextWrapper.EndReachedException e) {
            //not a single statement, but no statement at all XD
            throw new CompileException("expected: statement found: nothing", text.getPosition());
        }
        //list of all the statements
        ArrayList<Statement> statements = new ArrayList<>();
        while (!allStatementsFound) {
            if (singleStatement) {
                allStatementsFound = true;
            }
            //check if the end is reached
            char possibleEndBracket;
            try {
                possibleEndBracket = text.getNextNonWhitespaceChar();
            } catch (ProgramTextWrapper.EndReachedException e) {
                throw new CompileException("expected: " + (singleStatement ? "statement" : "}") + " found: nothing", text.getPosition());
            }
            if (possibleEndBracket == '}') {
                //it is over
                if (singleStatement) throw new CompileException("illegal character: }", text.getPosition());
                else {
                    allStatementsFound = true;
                }
            } else {
                //reset position
                text.moveBackward();
                //here it really begins
                //it has to filter out for while and if and empty statements, it can pass the rest to compileSingleStatement
                char possibleStatementEnd;
                try {
                    possibleStatementEnd = text.getNextNonWhitespaceChar();
                } catch (ProgramTextWrapper.EndReachedException e) {
                    throw new CompileException("expected: ; found: nothing", text.getPosition());
                }
                //check if it is an empty statement, if yes just do nothing
                if (possibleStatementEnd != ';') {
                    text.moveBackward();
                    String special;
                    ProgramPosition beforeSpecial = text.getPosition();
                    try {
                        special = text.getNextWord();
                    } catch (CompileException e) {
                        //just assign an empty string, this is now the problem of compileSingleStatement
                        special = "";
                        text.setPosition(beforeSpecial);
                    }
                    switch (special) {
                        case "for":
                            statements.add(compileForLoop());
                            break;
                        case "if":
                            statements.add(compileIfCondition());
                            break;
                        case "while":
                            statements.add(compileWhileLoop());
                            break;
                        default:
                            text.setPosition(beforeSpecial);
                            statements.add(compileSingleStatement(true, true));
                            //check for ; at the end
                            try {
                                char c = text.getNextNonWhitespaceChar();
                                if (c != ';') throw new CompileException("expected: ; found " + c, text.getPosition());
                            } catch (ProgramTextWrapper.EndReachedException e) {
                                throw new CompileException("expected: ; found: nothing", text.getPosition());
                            }
                            break;
                    }
                }
            }
        }
        //end the stack level
        stack.clearStackLevel();
        return statements.toArray(Statement[]::new);
    }

    //compile a single Statement
    //you can submit a boolean if it allows the return statement or not
    //you can submit a boolean if it allows the declaration of methods or not
    //after compile, text position is at the char which made it to stop
    //there MUST be a statement, empty ; are a problem of compileStatementBlock
    private Statement compileSingleStatement(boolean allowsReturn, boolean allowsDeclaration) throws CompileException {
        //get the first word and check for keywords
        ProgramPosition begin = text.getPosition();
        String firstWord;
        try {
            firstWord = text.getNextWord();
        } catch (CompileException e) {
            //this is a problem of compileCompleteExpression
            firstWord = "";
        }
        //handle all the default stuff
        if (firstWord.equals("return")) {
            //check if returns are allowed
            if (!allowsReturn) throw new CompileException("return statement is not allowed here", text.getPosition());
            //check if the return type is void
            if (actualMethod.getReturnType().equals(DataType.VOID)) {
                return new ReturnValueStatement(actualMethod, null);
            } else {
                Expression assignToReturn = compileCompleteExpression();
                //correct type if possible
                if (actualMethod.getReturnType().equals(DataType.INTEGER) && assignToReturn.getType().equals(DataType.FLOAT))
                    assignToReturn = new FloatToIntegerCast(assignToReturn);
                else if (actualMethod.getReturnType().equals(DataType.FLOAT) && assignToReturn.getType().equals(DataType.INTEGER))
                    assignToReturn = new IntegerToFloatCast(assignToReturn);
                //check if type is correct
                if (!actualMethod.getReturnType().equals(assignToReturn.getType()))
                    throw new CompileException("wrong return type", text.getPosition());
                //everything is ok, return the expression
                return new ReturnValueStatement(actualMethod, assignToReturn);
            }
        } else if (DataType.containsDataType(firstWord)) {
            //it is a declaration of a variable
            //check if this is allowed here
            if (!allowsDeclaration)
                throw new CompileException("it is not allowed to declare a variable here", text.getPosition());
            //get the name of the variable
            String variableName = text.getNextWord();
            //check if name already exists
            if (stack.exists(variableName))
                throw new CompileException("a variable with the name " + variableName + " already exists", text.getPosition());
            //check if the name is a keyword
            if (CompilerHelper.isReservedKeyword(variableName))
                throw new CompileException("reserved keyword", text.getPosition());
            //add variable
            //System.out.println("add to stack: " + variableName + ", " + firstWord);
            stack.addToStack(variableName, DataType.fromName(firstWord));
            //check if it ends here or if there is an init
            Expression initExpression = null;
            try {
                char possibleAssignment = text.getNextNonWhitespaceChar();
                if (possibleAssignment == '=') {
                    //it is an assignment
                    initExpression = compileCompleteExpression();
                    //correct type if possible
                    if (firstWord.equals(DataType.INTEGER_KW) && initExpression.getType().equals(DataType.FLOAT))
                        initExpression = new FloatToIntegerCast(initExpression);
                    else if (firstWord.equals(DataType.FLOAT_KW) && initExpression.getType().equals(DataType.INTEGER))
                        initExpression = new IntegerToFloatCast(initExpression);
                    //check if type is correct
                    if (!initExpression.getType().equals(DataType.fromName(firstWord)))
                        throw new CompileException("can't assign " + initExpression.getType() + " to " + DataType.fromName(firstWord), text.getPosition());

                } else {
                    //the statement ends after that
                    text.moveBackward();
                }
            } catch (ProgramTextWrapper.EndReachedException e) {
                //this is no problem here (in reality it will be a problem, but not of this method)
            }
            //everything is correct, create and return the correct statement
            DefineValueStatement define = new DefineValueStatement(DataType.fromName(firstWord));
            define.setName(variableName);
            define.setValueExpression(initExpression);
            return define;
        } else {
            //there is no other normal statement, so it also has to be an expression, so it's the problem of the expression compiler
            text.setPosition(begin);
            Expression fromExpressionCompiler = compileCompleteExpression();
            if (fromExpressionCompiler instanceof Statement) return (Statement) fromExpressionCompiler;
            else throw new CompileException("expected: statement", begin);
        }
    }

    //text is after the if word
    private Statement compileIfCondition() throws CompileException {
        IfConditionStatement ics = new IfConditionStatement();
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != '(') throw new CompileException("expected: ( found: " + c, text.getPosition());
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ( found: nothing", text.getPosition());
        }
        Expression possibleCondition = compileCompleteExpression();
        //check if type is boolean
        if (!possibleCondition.getType().equals(DataType.BOOLEAN))
            throw new CompileException("expected: expression of type boolean found: expression of type " + possibleCondition.getType(), text.getPosition());
        ics.setCondition(possibleCondition);
        //check if there is the end bracket
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != ')') throw new CompileException("expected: ) found: " + c, text.getPosition());
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ) found: nothing", text.getPosition());
        }
        //assign a statement block
        ics.setStatements(compileStatementBlock());
        //check if there is an else block
        ProgramPosition beforePossibleElse = text.getPosition();
        boolean foundElse = false;
        try {
            String s = text.getNextWord();
            foundElse = s.equals("else");
        } catch (CompileException e) {
            //reset position
            text.setPosition(beforePossibleElse);
        }
        if (foundElse) {
            //there is an else condition
            //just compile it
            ics.setElseStatement(new StatementBlock(compileStatementBlock()));
        } else {
            //reset position
            text.setPosition(beforePossibleElse);
        }

        return ics;
    }

    //text is after the while word
    private Statement compileWhileLoop() throws CompileException {
        WhileLoopStatement wls = new WhileLoopStatement();
        //check for correct begin
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != '(') throw new CompileException("expected: ( found: " + c, text.getPosition());
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ( found: nothing", text.getPosition());
        }
        Expression possibleCondition = compileCompleteExpression();
        //check if type is boolean
        if (!possibleCondition.getType().equals(DataType.BOOLEAN))
            throw new CompileException("expected: expression of type boolean found: expression of type " + possibleCondition.getType(), text.getPosition());
        wls.setRunCondition(possibleCondition);
        //check for correct end
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != ')') throw new CompileException("expected: ) found: " + c, text.getPosition());
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ) found: nothing", text.getPosition());
        }
        //get the body
        wls.setStatements(compileStatementBlock());
        return wls;
    }

    //text is after the for word
    private Statement compileForLoop() throws CompileException {
        //the most complex structure until now
        //begin stack level
        stack.beginStackLevel();
        ForLoopStatement fls = new ForLoopStatement();
        //check for correct begin
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != '(') throw new CompileException("expected: ( found: " + c, text.getPosition());
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ( found: nothing", text.getPosition());
        }
        //try to get init statement
        Statement initStatement = null;
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != ';') {
                //there is an init statement
                text.moveBackward();
                initStatement = compileSingleStatement(false, true);
                //check that there is the correct char now
                c = text.getNextNonWhitespaceChar();
                if (c != ';') throw new CompileException("expected: ; found: " + c, text.getPosition());
            }
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ; found: nothing", text.getPosition());
        }
        Expression possibleCondition;
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != ';') {
                //there is a condition
                text.moveBackward();
                possibleCondition = compileCompleteExpression();
                //check that type is correct
                if (!possibleCondition.getType().equals(DataType.BOOLEAN))
                    throw new CompileException("expected: expression of type boolean found: expression of type " + possibleCondition.getType(), text.getPosition());
                //check that there is the correct char now
                c = text.getNextNonWhitespaceChar();
                if (c != ';') throw new CompileException("expected: ; found: " + c, text.getPosition());
            } else
                throw new CompileException("expected: expression of type boolean found: nothing", text.getPosition());
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ; found: nothing", text.getPosition());
        }
        //try to get step statement
        Statement stepStatement = null;
        try {
            char c = text.getNextNonWhitespaceChar();
            if (c != ')') {
                //there is an init statement
                text.moveBackward();
                stepStatement = compileSingleStatement(false, false);
                //check that there is the correct char now
                c = text.getNextNonWhitespaceChar();
                if (c != ')') throw new CompileException("expected: ) found: " + c, text.getPosition());
            }
        } catch (ProgramTextWrapper.EndReachedException e) {
            throw new CompileException("expected: ) found: nothing", text.getPosition());
        }
        fls.setInitStatement(initStatement);
        fls.setRunCondition(possibleCondition);
        fls.setStepStatement(stepStatement);
        //get the body
        fls.setStatements(compileStatementBlock());
        //end stack level
        stack.clearStackLevel();
        return fls;
    }

}
