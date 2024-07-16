main(args)

fun main(args: Array<String>? = null) {
    println("received arguments => ${args?.joinToString { it }}")
    val body = args?.first()
    val author = body?.getFieldForTag("author")
    val reference = body?.getFieldForTag("reference")
    println("{ \"author\": \"$author\", \"reference\": \"$reference\" }")
}

fun String.getStringForField(field: String) = this.substring(this.indexOf(field), this.indexOf("\n", this.indexOf(field)))

fun String.getFieldForTag(field: String): String =
    this.substring(this.indexOf("<$field>"), this.indexOf("</$field>")).replace("<$field>", "")
