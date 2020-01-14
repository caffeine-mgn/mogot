package pw.binom.material.psi

enum class TokenType {
    ID,
    WHITE_SPACE,
    NUMBER,
    BAD_CHARACTER,
    OP_DIV,
    DERECTIVE,
    OP_TIMES,
    ASSIGN,
    COMMA,
    OP_PLUS,
    OP_MINUS,
    DOT,
    NOT,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,
    LEFT_INDEX,
    RIGHT_INDEX,
    OP_LT,
    OP_GT,
    LEFT_BRACE,
    RIGHT_BRACE,
    END_LINE,
    CMD_SEPARATOR,
    COMMENT_LINE,
    ASSIGN_DIV,
    ASSIGN_TIMES,
    ASSIGN_PLUS,
    UNARY_PLUS,
    ASSIGN_MINUS,
    UNARY_MINUS,
    INC,
    IF,
    ELSE,
    DEC,
    IN,
    OP_LE,
    OP_GE,
    OP_NE,
    OP_EQ,
    FOR,
    OUT,
    INT,
    COMMENT_BLOCK,
    VEC2,
    BOOL,
    VEC3,
    VEC4,
    MAT3,
    MAT4,
    VOID,
    FLOAT,
    DOUBLE,
    RETURN,
    UNIFORM,
    PROPERTY,
    VERTEX,
    NORMAL,
    UV,
    MODEL,
    PROJECTION,
    CLASS,
    TRUE,
    FALSE,
    STRING;

    val isPrimitive
        get() = when (this) {
            VEC2, VEC3, VEC4, INT, FLOAT, DOUBLE, MAT3, MAT4, BOOL -> true
            else -> false
        }

    val isOperation
        get() = when (this) {
            OP_PLUS, OP_MINUS, OP_LE, OP_GE, OP_LT, OP_GT, OP_DIV, OP_TIMES, OP_NE, OP_EQ -> true
            else -> false
        }

    val isAssign
        get() = when (this) {
            ASSIGN_DIV,
            ASSIGN_MINUS,
            ASSIGN_PLUS,
            ASSIGN_TIMES,
            ASSIGN -> true
            else -> false
        }
}