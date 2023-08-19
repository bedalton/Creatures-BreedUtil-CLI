package com.bedalton.creatures.breed.converter.cli.internal
internal fun <K,V> List<Map<K, V>>.flatten(): Map<K, V> {
    return this.flatMap { it.entries }
        .associate { it.key to it.value }
}