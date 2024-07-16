fun main(args: Array<String>) {
    val body = args.first()
    val pages =
        (1..5).map {
            val description = body.getStringForField("page_$it")
            "{ title: \"\", description: $description, thumbnailURL: \"\" }"
        }
    println("[ $pages ]")
}

fun String.getStringForField(field: String) = this.substring(this.indexOf(field), this.indexOf("\n", this.indexOf(field)))
