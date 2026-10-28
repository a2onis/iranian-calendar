package ir.gahshomar.parsi.data

enum class ArticleCategory(val label: String, val emoji: String) {
    NOWRUZ("نوروز و آیین‌ها", "🌱"),
    ZOROASTRIAN("دین زرتشتی", "🔥"),
    ANCIENT("ایران باستان", "🏛"),
    CULTURE("فرهنگ و هنر", "🎨"),
    CALENDAR("گاه‌شماری", "☀")
}

data class Article(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ArticleCategory,
    val paragraphs: List<String>,
    val bullets: List<Pair<String, String>> = emptyList(),
    val quote: String? = null,
    val quoteSource: String? = null
)

object Articles {

    val ALL: List<Article> = buildList {
        addAll(ArticlesNowruz.list())
        addAll(ArticlesZoroastrian.list())
        addAll(ArticlesAncient.list())
        addAll(ArticlesCulture.list())
    }

    private val byId: Map<String, Article> = ALL.associateBy { it.id }

    fun byId(id: String?): Article? = if (id == null) null else byId[id]

    fun byCategory(category: ArticleCategory): List<Article> = ALL.filter { it.category == category }

    fun search(query: String): List<Article> {
        if (query.isBlank()) return ALL
        val q = normalize(query)
        return ALL.filter {
            normalize(it.title).contains(q) || normalize(it.subtitle).contains(q) ||
                it.paragraphs.any { p -> normalize(p).contains(q) }
        }
    }

    private fun normalize(text: String): String = text
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace("\u200c", "")
        .replace(" ", "")
        .lowercase()
}
