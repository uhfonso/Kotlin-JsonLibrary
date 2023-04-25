import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestsLibraryJSON {

    private var jobject = JSONObject()
    private var studentArray = JSONArray()
    private var auxArray = JSONArray()
    private var student1 = JSONObject()
    private var student2 = JSONObject()
    private var student3 = JSONObject()

    /*
     * Creates an example JSON Element hierarchy to be used in testing,
     * Executed before each test to guarantee consistent results.
     */
    @BeforeTest
    fun createHierarchy() {
        jobject = JSONObject()
        jobject.addElement("uc", JSONString("PA"))
        jobject.addElement("ects", JSONNumber(6.0))
        jobject.addElement("data-exame", JSONNull())

        studentArray = JSONArray()
        jobject.addElement("inscritos", studentArray)

        student1 = JSONObject()
        studentArray.addElement(student1)
        student1.addElement("numero", JSONNumber(101101))
        student1.addElement("nome", JSONString("Dave Farley"))
        student1.addElement("internacional", JSONBoolean(true))

        student2 = JSONObject()
        studentArray.addElement(student2)
        student2.addElement("numero", JSONNumber(101102))
        student2.addElement("nome", JSONString("Martin Fowler"))
        student2.addElement("internacional", JSONBoolean(true))

        student3 = JSONObject()
        studentArray.addElement(student3)
        student3.addElement("numero", JSONNumber(26503))
        student3.addElement("nome", JSONString("André Santos"))
        student3.addElement("internacional", JSONBoolean(false))
    }

    @Test
    fun testHierarchy() {
        val jarray = JSONArray()
        jarray.addElement(JSONString("E1"))
        jarray.addElement(JSONNumber(1))

        assertEquals("{numero=101101, nome=\"Dave Farley\", internacional=true}", student1.toString())
        assertEquals("[\"E1\", 1]", jarray.toString())
        assertEquals("{uc=\"PA\", ects=6.0, data-exame=null, inscritos=[{numero=101101, nome=\"Dave Farley\", internacional=true}, " +
                "{numero=101102, nome=\"Martin Fowler\", internacional=true}, {numero=26503, nome=\"André Santos\", internacional=false}]}", jobject.toString())
        assertEquals("{\n\t\"numero\" : 26503,\n\t\"nome\" : \"André Santos\",\n\t\"internacional\" : false\n}", student3.getStructure())
        assertEquals("[\n\t\"E1\",\n\t1\n]", jarray.getStructure())
    }

    @Test
    fun testSearch() {
        val jarray2 = JSONArray()
        jarray2.addElement(JSONString("E1"))
        jarray2.addElement(JSONNumber(1))

        val result = mutableListOf<JSONElement>()
        student1.elements["numero"]?.let { result.add(it) }
        student2.elements["numero"]?.let { result.add(it) }
        student3.elements["numero"]?.let { result.add(it) }

        val students = mutableListOf(student1, student2, student3)

        assertEquals(result, jobject.getValuesByProperty("numero"))
        assertEquals(mutableListOf(), jobject.getValuesByProperty("raiz"))
        assertEquals(mutableListOf(), jobject.getValuesByProperty(""))
        assertIs<JSONArray>(jobject.getValuesByProperty("inscritos")[0])
        assertIs<List<JSONElement>>((jobject.getValuesByProperty("inscritos")[0] as JSONArray).elements)
        assertEquals(students, (jobject.getValuesByProperty("inscritos")[0] as JSONArray).elements as List<JSONElement> )

        assertEquals(students, jobject.getJSONObjectWithProperty(listOf("numero", "nome")))
        assertEquals(mutableListOf(), jobject.getJSONObjectWithProperty(listOf("numero", "raiz")))
        assertEquals(students, jobject.getJSONObjectWithProperty(listOf("numero", "internacional")))
        assertEquals(mutableListOf(jobject), jobject.getJSONObjectWithProperty(listOf("data-exame")))
        assertEquals(mutableListOf(jobject), jobject.getJSONObjectWithProperty(listOf("inscritos")))
        assertEquals(mutableListOf(), jobject.getJSONObjectWithProperty(listOf()))
        assertEquals(students, jobject.getJSONObjectWithProperty(listOf("numero", "numero")))

    }

    @Test
    fun testVerifications() {
        assertTrue(jobject.verifyStructure("numero", JSONNumber::class))
        assertTrue(jobject.verifyStructure("nome", JSONString::class))
        assertTrue(jobject.verifyStructure("internacional", JSONBoolean::class))
        assertTrue(jobject.verifyStructure("inscritos", JSONArray::class))
        assertTrue(jobject.verifyStructure("data-exame", JSONNull::class))
        assertFalse(jobject.verifyStructure("internacional", JSONArray::class))
        assertFalse(jobject.verifyStructure("numero", JSONString::class))

        assertTrue(jobject.verifyStructure("inexistente", JSONString::class))

        var student4 = JSONObject()
        studentArray.addElement(student4)
        student4.addElement("numero", JSONString("teste"))
        student4.addElement("nome", JSONString("André Santos"))
        student4.addElement("internacional", JSONBoolean(false))

        assertFalse(jobject.verifyStructure("numero", JSONNumber::class))

    }

    @Test
    fun testArrayVerification() {
        assertTrue(jobject.verifyArrayEquality("inscritos"))
        assertTrue(jobject.verifyArrayEqualityAlt("inscritos"))

        // Propriedades a menos + Classes Iguais
        auxArray = JSONArray()
        jobject.addElement("auxiliar", auxArray)

        var student5 = JSONObject()
        auxArray.addElement(student5)
        student5.addElement("numero", JSONString("teste"))
        student5.addElement("internacional", JSONBoolean(false))

        var student6 = JSONObject()
        auxArray.addElement(student6)
        student6.addElement("numero", JSONString("teste"))
        student6.addElement("nome", JSONString("André Santos"))
        student6.addElement("internacional", JSONBoolean(false))

        assertFalse(jobject.verifyArrayEquality("auxiliar"))
        assertFalse(jobject.verifyArrayEqualityAlt("auxiliar"))

        // Mesmas propriedades + Classes Diferentes
        var auxArray2 = JSONArray()
        jobject.addElement("auxiliar2", auxArray2)

        var student7 = JSONObject()
        auxArray2.addElement(student7)
        student7.addElement("numero", JSONString("teste"))
        student7.addElement("nome", JSONNumber(10))
        student7.addElement("internacional", JSONBoolean(false))

        var student8 = JSONObject()
        auxArray2.addElement(student8)
        student8.addElement("numero", JSONString("teste"))
        student8.addElement("nome", JSONString("André Santos"))
        student8.addElement("internacional", JSONBoolean(false))

        assertFalse(jobject.verifyArrayEquality("auxiliar2"))
        assertFalse(jobject.verifyArrayEqualityAlt("auxiliar2"))

        // Propriedades diferentes + Classes Diferentes
        var auxArray3 = JSONArray()
        jobject.addElement("auxiliar3", auxArray3)

        var student9 = JSONObject()
        auxArray3.addElement(student9)
        student9.addElement("numero", JSONString("teste"))
        student9.addElement("valido", JSONBoolean(true))
        student9.addElement("internacional", JSONBoolean(false))

        var student10 = JSONObject()
        auxArray3.addElement(student10)
        student10.addElement("numero", JSONString("teste"))
        student10.addElement("nome", JSONString("André Santos"))
        student10.addElement("internacional", JSONBoolean(false))

        assertFalse(jobject.verifyArrayEquality("auxiliar3"))
        assertFalse(jobject.verifyArrayEqualityAlt("auxiliar3"))

        // Array com objetos dentro dos objetos
        var auxArray4 = JSONArray()
        jobject.addElement("auxiliar4", auxArray4)

        var student11 = JSONObject()
        auxArray4.addElement(student11)
        student11.addElement("numero", JSONNumber(93178))
        student11.addElement("nome", JSONString("Afonso Sampaio"))
        student11.addElement("internacional", JSONBoolean(false))
        student11.addElement("extra", student1)

        var student12 = JSONObject()
        auxArray4.addElement(student12)
        student12.addElement("numero", JSONNumber(93179))
        student12.addElement("nome", JSONString("Samuel"))
        student12.addElement("internacional", JSONBoolean(false))
        student12.addElement("extra", student2)

        assertTrue(jobject.verifyArrayEquality("auxiliar4"))
        assertTrue(jobject.verifyArrayEqualityAlt("auxiliar4"))


        // Array com objetos de estruturas diferentes dentro dos objetos
        var auxArray5 = JSONArray()
        jobject.addElement("auxiliar5", auxArray5)

        var student13 = JSONObject()
        auxArray5.addElement(student13)
        student13.addElement("numero", JSONNumber(93178))
        student13.addElement("nome", JSONString("Afonso Sampaio"))
        student13.addElement("internacional", JSONBoolean(false))
        student13.addElement("extra", student1)

        var student14 = JSONObject()
        auxArray5.addElement(student14)
        student14.addElement("numero", JSONNumber(93179))
        student14.addElement("nome", JSONString("Samuel"))
        student14.addElement("internacional", JSONBoolean(false))
        student14.addElement("extra", student5)

        //assertFalse(jobject.verifyArrayEquality("auxiliar5"))
        assertFalse(jobject.verifyArrayEqualityAlt("auxiliar5"))


        // Array com elementos
        val jarray = JSONArray()
        jobject.addElement("differentarray", jarray)
        jarray.addElement(JSONString("E1"))
        jarray.addElement(JSONNumber(1))

        assertFalse(jobject.verifyArrayEquality("differentarray"))
        assertFalse(jobject.verifyArrayEqualityAlt("differentarray"))

        val jarray2 = JSONArray()
        jobject.addElement("simplearray", jarray2)
        jarray2.addElement(JSONNumber(1))
        jarray2.addElement(JSONNumber(2))

        assertTrue(jobject.verifyArrayEquality("simplearray"))
        assertTrue(jobject.verifyArrayEqualityAlt("simplearray"))

        // Array com elementos e objetos
        val jarray3 = JSONArray()
        jobject.addElement("mixedarray", jarray3)
        jarray3.addElement(JSONNumber(1))
        jarray3.addElement(student1)

        assertFalse(jobject.verifyArrayEquality("mixedarray"))
        assertFalse(jobject.verifyArrayEqualityAlt("mixedarray"))

        // propriedade nao correspondente a um array
        assertTrue(jobject.verifyArrayEquality("uc"))
        assertTrue(jobject.verifyArrayEqualityAlt("uc"))

        assertTrue(jobject.verifyArrayEquality("naoexiste"))
        assertTrue(jobject.verifyArrayEqualityAlt("naoexiste"))
    }

    /*
     * Tests the inference via reflection using the original test hierarchy
     * as well as a custom hierarchy to verify as many scenarios as possible.
     */
    @Test
    fun testInference() {
        val classObject = ClassObject(UC.PA, 6.0, null)
        classObject.addInscrito(StudentObject(101101, "Dave Farley", true))
        classObject.addInscrito(StudentObject(101102, "Martin Fowler", true))
        classObject.addInscrito(StudentObject(26503, "André Santos", false))

        val jsonClassObject: JSONObject = classObject.toJson()
        assertEquals(jobject.getStructure(), jsonClassObject.getStructure())

        val customClassObject = CustomClassObject(
                StudentObject(0, "A", true),
                listOf(1, StudentObject(1, "S", false), listOf(1, 2, 3)),
                mapOf("One point five" to 1.5, "Two point two" to 2.2),
                1, true, 'x', "stringValue", UC.PGMV, "excluded", false, 99)

        val customJsonElements = customClassObject.toJson().elements

        assertIs<JSONObject>(customJsonElements["student"])
        val list = customJsonElements["list"]
        assertIs<JSONArray>(list)
        assertTrue(list.elements[0] is JSONNumber && list.elements[1] is JSONObject && list.elements[2] is JSONArray)
        val mapObject = customJsonElements["map"]
        assertIs<JSONObject>(mapObject)
        assertTrue(mapObject.elements["One point five"] is JSONNumber && mapObject.elements["Two point two"] is JSONNumber)
        assertIs<JSONNumber>(customJsonElements["number"])
        assertEquals(1, (customJsonElements["number"] as JSONNumber).value)
        assertIs<JSONBoolean>(customJsonElements["boolean"])
        assertEquals(true, (customJsonElements["boolean"] as JSONBoolean).value)
        assertIs<JSONString>(customJsonElements["character"])
        assertEquals("x", (customJsonElements["character"] as JSONString).value)
        assertIs<JSONString>(customJsonElements["string"])
        assertEquals("stringValue", (customJsonElements["string"] as JSONString).value)
        assertIs<JSONString>(customJsonElements["enum"])
        assertEquals("PGMV", (customJsonElements["enum"] as JSONString).value)
        assertTrue(!customJsonElements.contains("excluded"))
        assertTrue(!customJsonElements.contains("truth") && customJsonElements.contains("lie"))
        assertIs<JSONString>(customJsonElements["numberAsString"])
        assertEquals("\"99\"", (customJsonElements["numberAsString"] as JSONString).toString())
    }
}
