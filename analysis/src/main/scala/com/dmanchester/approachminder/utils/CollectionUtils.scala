package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithoutbehavior.AircraftCategory

object CollectionUtils {

  /**
   * In a non-empty collection of categories, determine the most-common one that is "non-blank" (i.e., neither
   * "NoInfoAtAll" nor "NoADSBEmitterCategoryInfo"; see AircraftCategory.blank).
   *
   * If multiple non-blank categories are equally common, pick the one that is alphabetically first by class name. (This
   * ensures deterministic behavior regardless of the categories' ordering.)
   *
   * @param categories The collection of categories.
   * @throws java.lang.IllegalArgumentException If the collection is empty.
   * @return The most-common non-blank category, wrapped in Some; or None, if all categories are blank.
   */
  @throws(classOf[IllegalArgumentException])
  def mostCommonNonBlankCategoryInNonEmptyCollection(categories: Iterable[AircraftCategory]): Option[AircraftCategory] = {

    if (categories.isEmpty) {
      throw new IllegalArgumentException("'categories' must not be empty!")
    }

    val nonBlankCategories = categories.filter(!AircraftCategory.blank.contains(_))

    Option.when(nonBlankCategories.nonEmpty) {

      val categoriesAndCounts = nonBlankCategories.groupBy(identity).map { case (category, categoryOccurrences) =>
        (category, categoryOccurrences.size)
      }

      // Pick category and count "a" if:
      //
      //   * a's count is higher than b's; or,
      //   * their counts are the same, but "a" comes first alphabetically.
      //
      // Otherwise, pick "b".
      val mostCommonCategoryWithCount = categoriesAndCounts.reduce { (a, b) =>
        if (a._2 > b._2 || (a._2 == b._2 && a._1.getClass.getSimpleName < b._1.getClass.getSimpleName))
          a
        else
          b
      }

      mostCommonCategoryWithCount._1
    }
  }
}
