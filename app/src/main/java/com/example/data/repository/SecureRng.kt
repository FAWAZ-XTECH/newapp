package com.example.data.repository

import java.security.SecureRandom

/**
 * Cryptographically strong random number generator for fair demo mini-game outcomes.
 * Outcomes are purely probabilistic and represent virtual demo points with zero monetary value.
 */
object SecureRng {
  private val random = SecureRandom()

  fun nextInt(bound: Int): Int {
    require(bound > 0) { "Bound must be positive" }
    return random.nextInt(bound)
  }

  fun nextIntRange(minInclusive: Int, maxInclusive: Int): Int {
    require(maxInclusive >= minInclusive) { "Invalid range" }
    return minInclusive + random.nextInt(maxInclusive - minInclusive + 1)
  }

  fun <T> shuffled(list: List<T>): List<T> {
    val mutable = list.toMutableList()
    for (i in mutable.size - 1 downTo 1) {
      val j = nextInt(i + 1)
      val temp = mutable[i]
      mutable[i] = mutable[j]
      mutable[j] = temp
    }
    return mutable
  }
}
