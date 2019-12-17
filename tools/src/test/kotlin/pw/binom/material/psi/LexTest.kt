package pw.binom.material.psi

import org.junit.Assert
import org.junit.Test
import java.io.StringReader
import kotlin.reflect.KClass
import kotlin.test.assertEquals

fun makeStream(txt: String): LexStream<TokenType> {
    val lexer = GLSLLexer(StringReader(txt))
    return LexStream {
        lexer.next()?.let {
            if (it == TokenType.STRING)
                Parser.StringElement(lexer.stringLiteral(), it, lexer.text, lexer.line, lexer.column, lexer.position)
            else
                LexStream.Element(it, lexer.text, lexer.line, lexer.column, lexer.position)
        }
    }
}

class LexTest {

    @Test
    fun lexerTest(){
        val l = GLSLLexer(StringReader("vec3 vec4ee"))
        l.next()
        l.position.eq(0)
        l.text.length.eq(4)

        l.next()
        l.position.eq(4)
        l.text.length.eq(1)

        l.next()
        l.position.eq(5)
        l.text.length.eq(6)
    }

    @Test
    fun testString() {
        val e = Expression.read(makeStream(""""Hello from String\n"""")).notNull()
        e as StringExpression
        assertEquals(""""Hello from String\n"""", e.text)
        assertEquals("Hello from String\n", e.string)
    }

    @Test
    fun test() {

        val ee = Expression.read(makeStream("vec3(0,0,0)"))
                .notNull()
                .cast(MethodCallExpression::class)

        MethodCallExpression.read(null, makeStream("vec3(0,0,0)"))
                .notNull()
                .also {
                    it.method.eq("vec3")
                    it.args.size.eq(3)
                    it.args.forEach {
                        (it is NumberExpression).eq(true)
                        it as NumberExpression
                        it.value.eq("0")
                    }
                }

        StatementBlock.read(makeStream("""
                        {
                            return vec3(0,0,0)
                        }
        """)).notNull().cast(StatementBlock::class).also {
            it.statements.size.eq(1)
            it.statements[0].also {
                it.cast(ReturnStatement::class)
            }
        }

        run {
            val stream = makeStream("""
                        vvv(0,0,0)
                        vvv(0,0,0)
        """)

            Expression.read(stream).notNull().cast(MethodCallExpression::class)
            Expression.read(stream).notNull().cast(MethodCallExpression::class)
        }

        run {
            val stream = makeStream("""
                        return aaa(0,1,2)
                        return bbb(3,4,5)
        """)

            ReturnStatement.read(stream).notNull().cast(ReturnStatement::class)
            ReturnStatement.read(stream).notNull().cast(ReturnStatement::class)
        }

        run {
            val stream = makeStream("doit(0,0,0)")

            stream.safe {
                stream.cursor.eq(0)
                SimpleExpression.read(null, stream).notNull().cast(MethodCallExpression::class)
                stream.cursor.eq(8)
                null
            }
            stream.cursor.eq(0)

            stream.safe {
                stream.cursor.eq(0)
                SubjectExpression.read(stream).notNull().cast(MethodCallExpression::class)
                null
            }
            stream.cursor.eq(0)

            stream.cursor.eq(0)
            OperationExpression.read(stream).mustNull()
            stream.cursor.eq(0)
        }
    }

    @Test
    fun returnTest() {
        run {
            val stream = makeStream("return 2")
            ReturnStatement.read(stream).notNull()
            stream.cursor.eq(3)
        }

        run {
            val stream = makeStream("return vec(1,2,3)")
            ReturnStatement.read(stream).notNull()
            stream.cursor.eq(10)
        }
    }

    @Test
    fun methodDefineTest() {

        run {
            val stream = makeStream("""
                        aa vertex(){
                        }
                        
                        cc fragment(){
                        }
        """)

            GlobalMethod.read(stream).notNull().cast(GlobalMethod::class)
            GlobalMethod.read(stream).notNull().cast(GlobalMethod::class)
        }
    }

    @Test
    fun statementBlockTest() {
        run {
            val stream = makeStream("""
                        {
                        }
                        
                        {
                        }
        """)

            StatementBlock.read(stream).notNull()
            StatementBlock.read(stream).notNull()
        }
    }
}

fun <T : Any?> T?.notNull(): T {
    Assert.assertNotNull(this)
    return this!!
}

fun <T : Any?> T?.mustNull(): T? {
    Assert.assertNull(this)
    return this
}

fun Boolean.eq(other: Boolean): Boolean {
    Assert.assertEquals(other, this)
    return this
}

fun Int.eq(other: Int): Int {
    Assert.assertEquals(other, this)
    return this
}

fun String.eq(other: String): String {
    Assert.assertEquals(other, this)
    return this
}

fun <T : Any> T.eq(other: T): T {
    Assert.assertEquals(other, this)
    return this
}

fun <T : Any, R : Any> T.cast(clazz: KClass<R>): R {
    Assert.assertTrue("Can't cast ${this::class.java.name} to ${clazz.java.name}", clazz.java.isInstance(this))
    return this as R
}