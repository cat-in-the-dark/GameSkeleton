package org.catinthedark.shared

/**
 * Очередь с весами.
 *
 * Чтобы сделать плавное перемещение объектов, координаты которых приходят по сети,
 * надо использовать магию lerp и всяких разных буферов, очередей и прочего.
 * Таким образом появилась идея специальной очереди, где у каждого элемента есть вес.
 * А забираются элементы из очереди так называемым “облегчением” элементов.
 * Сейчас объясню подробнее.
 *
 * Представьте себе обработчик задач, у которого есть определенный квант времени.
 * Вам нужно обработать все задачи на которые у вас хватит кванта времени.
 * Естественно задачи берутся из некоторой очереди.
 * Каждая задача имеет определенный вес трудозатрат времени.
 * Таким образом обработчик должен суметь обработать одну задачу частично или целиком несколько.
 *
 * Пример:
 *
 *   Состояние очереди: `[2, 3, 1, 8, 1, 1]`.
 *   Квант времени: 4.
 *   Означает, что обработчик сможет полностью обслужить задачу с весом 2 и частично с весом 3.
 *
 *   Состояние очереди: `[1, 1, 8, 1, 1]`.
 *   Квант времени: 3.
 *   Состояние очереди: `[7, 1, 1]`.
 *   И так далее.
 *
 * Зачем это нужно?
 * В данном случае, очередь хранит в себе сообщения о перемещениях объектов.
 * Чтобы объекты двигались плавно нужно использовать линейную интерполяцию
 * между начальным положением объекта и конечным, которое пришло с сервера.
 * Весом сообщения является задержка с которой оно получено.
 * Квант времени - это дельта времени на рисование предыдущего кадра.
 * Тогда интерфейс работы может выглядеть следующим образом:
 *
 * `val msgList = messagesQueue.poll(deltaTime)`
 *
 * `msgList(список)` - так как квант может вытянуть сразу несколько сообщений.
 * То как обрабатывать такие ситуации должен определять тот, кто вытаскивает из очереди, а не сама очередь.
 *
 * Дополнение - индекс перевеса.
 * В полне возможно что наша очередь будет заполнятся очень тяжелыми объектами.
 * Например, сообщение пришло с огромной задержкой.
 * Нет смысла вообще его как-то обрабатывать.
 * Поэтому предусмотренна защита от слишком тяжелых объектов.
 * Если очередь превосходит какой-то верхний предел,
 * то тяжелые старые объекты вывалятся из нее при следующем вытягиваниии не зависимо от кванта времени.
 */
class WeightedQueue<T>(
        private val overweight: Long = 100L
) {
    private val collection: MutableList<Weighted<T>> = arrayListOf()

    fun add(element: T, weight: Long) {
        collection.add(Weighted(
                weight = weight,
                actualWeight = 0L,
                payload = element))
    }

    fun pollOverweight(): List<Weighted<T>> {
        if (weight() > overweight) {
            return poll((overweight * 0.8).toLong())
        }
        return emptyList()
    }

    fun pollWithOverweight(weight: Long): List<Weighted<T>> {
        return pollOverweight() + poll(weight)
    }

    fun poll(weight: Long): List<Weighted<T>> {
        var haveWeight = weight
        val elements = collection.takeWhile { el ->
            if (haveWeight > 0L) {
                val need = el.delta()
                val w = if (need < haveWeight) {
                    need
                } else {
                    haveWeight
                }
                haveWeight -= w
                el.actualWeight += w
                true
            } else {
                false
            }
        }

        collection.removeAll {
            it.percentage() >= 0.98
        }

        return elements
    }

    fun weight(): Long {
        if (collection.isEmpty()) return 0
        return collection.map { it.weight - it.actualWeight }.reduceRight { sum, el -> sum + el }
    }

    val size: Int
        get() = collection.size

    fun clear() {
        collection.clear()
    }

    data class Weighted<out T>(
            val weight: Long, // in ms
            var actualWeight: Long, // in ms
            val payload: T
    ) {
        fun delta() = weight - actualWeight
        fun percentage(): Float {
            if (weight == 0L) return 1f
            val p = actualWeight.toFloat() / weight.toFloat()
            return if (p >= 1) {
                1f
            } else if (p <=0) {
                0f
            } else {
                p
            }
        }
    }
}