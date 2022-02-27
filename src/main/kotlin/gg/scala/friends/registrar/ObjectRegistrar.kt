package gg.scala.friends.registrar

/**
 * @author GrowlyX
 * @since 11/10/2021
 */
abstract class ObjectRegistrar<K, V> : Handler
{
    open val handle = mutableMapOf<K, V>()

    fun insert(k: K, v: V)
    {
        handle[k] = v
    }

    fun find(k: K): V? = handle[k]
    fun delete(k: K): V? = handle.remove(k)
}

interface Handler
{
    fun initialLoad()
}
