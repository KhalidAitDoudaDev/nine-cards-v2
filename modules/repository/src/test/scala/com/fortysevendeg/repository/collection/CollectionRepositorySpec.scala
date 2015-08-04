package com.fortysevendeg.repository.collection

import com.fortysevendeg.ninecardslauncher.repository.RepositoryExceptions.RepositoryException
import com.fortysevendeg.ninecardslauncher.repository.commons.{CollectionUri, ContentResolverWrapperImpl}
import com.fortysevendeg.ninecardslauncher.repository.model.Collection
import com.fortysevendeg.ninecardslauncher.repository.provider.CollectionEntity._
import com.fortysevendeg.ninecardslauncher.repository.provider._
import com.fortysevendeg.ninecardslauncher.repository.repositories._
import com.fortysevendeg.repository._
import org.specs2.matcher.DisjunctionMatchers
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import rapture.core.{Answer, Errata, Result}

trait CollectionRepositorySpecification
  extends Specification
  with DisjunctionMatchers
  with Mockito {

  trait CollectionRepositoryScope
    extends Scope {

    lazy val contentResolverWrapper = mock[ContentResolverWrapperImpl]
    lazy val collectionRepository = new CollectionRepository(contentResolverWrapper)
  }

  trait ValidCollectionRepositoryResponses
    extends DBUtils
    with CollectionRepositoryTestData {

    self: CollectionRepositoryScope =>

    contentResolverWrapper.insert(CollectionUri, createCollectionValues) returns testCollectionId

    contentResolverWrapper.deleteById(CollectionUri, testCollectionId) returns 1

    contentResolverWrapper.findById(
      nineCardsUri = CollectionUri,
      id = testCollectionId,
      projection = allFields)(
        f = getEntityFromCursor(collectionEntityFromCursor)) returns Some(collectionEntity)

    contentResolverWrapper.findById(
      nineCardsUri = CollectionUri,
      id = testNonExistingCollectionId,
      projection = allFields)(
        f = getEntityFromCursor(collectionEntityFromCursor)) returns None

    contentResolverWrapper.fetchAll(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = "",
      whereParams = Seq.empty,
      orderBy = s"$position asc")(
        f = getListFromCursor(collectionEntityFromCursor)) returns collectionEntitySeq

    contentResolverWrapper.fetch(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = s"$position = ?",
      whereParams = Seq(testPosition.toString),
      orderBy = "")(
        f = getEntityFromCursor(collectionEntityFromCursor)) returns Some(collectionEntity)

    contentResolverWrapper.fetch(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = s"$position = ?",
      whereParams = Seq(testNonExistingPosition.toString),
      orderBy = "")(
        f = getEntityFromCursor(collectionEntityFromCursor)) returns None

    contentResolverWrapper.fetch(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = s"$originalSharedCollectionId = ?",
      whereParams = Seq(testSharedCollectionId),
      orderBy = "")(
        f = getEntityFromCursor(collectionEntityFromCursor)) returns Some(collectionEntity)

    contentResolverWrapper.fetch(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = s"$originalSharedCollectionId = ?",
      whereParams = Seq(testNonExistingSharedCollectionId),
      orderBy = "")(
        f = getEntityFromCursor(collectionEntityFromCursor)) returns None

    contentResolverWrapper.updateById(CollectionUri, testCollectionId, createCollectionValues) returns 1
  }

  trait ErrorCollectionRepositoryResponses
    extends DBUtils
    with CollectionRepositoryTestData {

    self: CollectionRepositoryScope =>

    val contentResolverException = new RuntimeException("Irrelevant message")

    contentResolverWrapper.insert(CollectionUri, createCollectionValues) throws contentResolverException

    contentResolverWrapper.deleteById(CollectionUri, testCollectionId) throws contentResolverException

    contentResolverWrapper.findById(
      nineCardsUri = CollectionUri,
      id = testCollectionId,
      projection = allFields)(
        f = getEntityFromCursor(collectionEntityFromCursor)) throws contentResolverException

    contentResolverWrapper.fetchAll(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = "",
      whereParams = Seq.empty,
      orderBy = s"$position asc")(
        f = getListFromCursor(collectionEntityFromCursor)) throws contentResolverException

    contentResolverWrapper.fetch(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = s"$position = ?",
      whereParams = Seq(testPosition.toString),
      orderBy = "")(
        f = getEntityFromCursor(collectionEntityFromCursor)) throws contentResolverException

    contentResolverWrapper.fetch(
      nineCardsUri = CollectionUri,
      projection = allFields,
      where = s"$originalSharedCollectionId = ?",
      whereParams = Seq(testSharedCollectionId),
      orderBy = "")(
        f = getEntityFromCursor(collectionEntityFromCursor)) throws contentResolverException

    contentResolverWrapper.updateById(CollectionUri, testCollectionId, createCollectionValues) throws contentResolverException
  }

}

trait CollectionMockCursor
  extends MockCursor
  with DBUtils
  with CollectionRepositoryTestData {

  val cursorData = Seq(
    (NineCardsSqlHelper.id, 0, collectionSeq map (_.id), IntDataType),
    (position, 1, collectionSeq map (_.data.position), IntDataType),
    (name, 2, collectionSeq map (_.data.name), StringDataType),
    (collectionType, 3, collectionSeq map (_.data.collectionType), StringDataType),
    (icon, 4, collectionSeq map (_.data.icon), StringDataType),
    (themedColorIndex, 5, collectionSeq map (_.data.themedColorIndex), IntDataType),
    (appsCategory, 6, collectionSeq map (_.data.appsCategory getOrElse ""), StringDataType),
    (constrains, 7, collectionSeq map (_.data.constrains getOrElse ""), StringDataType),
    (originalSharedCollectionId, 8, collectionSeq map (_.data.originalSharedCollectionId getOrElse ""), StringDataType),
    (sharedCollectionId, 9, collectionSeq map (_.data.sharedCollectionId getOrElse ""), StringDataType),
    (sharedCollectionSubscribed, 10, collectionSeq map (item => if (item.data.sharedCollectionSubscribed getOrElse false) 1 else 0), IntDataType)
  )

  prepareCursor[Collection](collectionSeq.size, cursorData)
}

trait EmptyCollectionMockCursor
  extends MockCursor
  with DBUtils
  with CollectionRepositoryTestData {

  val cursorData = Seq(
    (NineCardsSqlHelper.id, 0, Seq.empty, IntDataType),
    (position, 1, Seq.empty, IntDataType),
    (name, 2, Seq.empty, StringDataType),
    (collectionType, 3, Seq.empty, StringDataType),
    (icon, 4, Seq.empty, StringDataType),
    (themedColorIndex, 5, Seq.empty, IntDataType),
    (appsCategory, 6, Seq.empty, StringDataType),
    (constrains, 7, Seq.empty, StringDataType),
    (originalSharedCollectionId, 8, Seq.empty, StringDataType),
    (sharedCollectionId, 9, Seq.empty, StringDataType),
    (sharedCollectionSubscribed, 10, Seq.empty, IntDataType)
  )

  prepareCursor[Collection](0, cursorData)
}

class CollectionRepositorySpec
  extends CollectionRepositorySpecification {

  "CollectionRepositoryClient component" should {

    "addCollection" should {

      "return a Collection object with a valid request" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.addCollection(data = createCollectionData).run.run

          result must beLike[Result[Collection, RepositoryException]] {
            case Answer(collection) =>
              collection.id shouldEqual testCollectionId
              collection.data.name shouldEqual testName
          }
        }

      "return a NineCardsException when a exception is thrown" in
        new CollectionRepositoryScope
          with ErrorCollectionRepositoryResponses {

          val result = collectionRepository.addCollection(data = createCollectionData).run.run

          result must beLike[Result[Collection, RepositoryException]] {
            case Errata(errors) =>
              errors.length should beGreaterThanOrEqualTo(1)
          }
        }
    }

    "deleteCollection" should {

      "return a successful result when a valid cache category id is given" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.deleteCollection(collection).run.run

          result must beLike[Result[Int, RepositoryException]] {
            case Answer(deleted) =>
              deleted shouldEqual 1
          }
        }

      "return a NineCardsException when a exception is thrown" in
        new CollectionRepositoryScope
          with ErrorCollectionRepositoryResponses {

          val result = collectionRepository.deleteCollection(collection).run.run

          result must beLike[Result[Int, RepositoryException]] {
            case Errata(errors) =>
              errors.length should beGreaterThanOrEqualTo(1)
          }
        }
    }

    "findCollectionById" should {

      "return a Collection object when a existing id is given" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.findCollectionById(id = testCollectionId).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Answer(maybeCollection) =>
              maybeCollection must beSome[Collection].which { collection =>
                collection.id shouldEqual testCollectionId
                collection.data.name shouldEqual testName
              }
          }
        }

      "return None when a non-existing id is given" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {
          val result = collectionRepository.findCollectionById(id = testNonExistingCollectionId).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Answer(maybeCollection) =>
              maybeCollection must beNone
          }
        }

      "return a NineCardsException when a exception is thrown" in
        new CollectionRepositoryScope
          with ErrorCollectionRepositoryResponses {

          val result = collectionRepository.findCollectionById(id = testCollectionId).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Errata(errors) =>
              errors.length should beGreaterThanOrEqualTo(1)
          }
        }
    }

    "fetchCollectionBySharedCollectionId" should {

      "return a Collection object when a existing shared collection id is given" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.fetchCollectionBySharedCollectionId(sharedCollectionId = testSharedCollectionId).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Answer(maybeCollection) =>
              maybeCollection must beSome[Collection].which { collection =>
                collection.id shouldEqual testCollectionId
                collection.data.name shouldEqual testName
              }
          }
        }

      "return None when a non-existing shared collection id is given" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.fetchCollectionBySharedCollectionId(sharedCollectionId = testNonExistingSharedCollectionId).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Answer(maybeCollection) =>
              maybeCollection must beNone
          }
        }

      "return a NineCardsException when a exception is thrown" in
        new CollectionRepositoryScope
          with ErrorCollectionRepositoryResponses {

          val result = collectionRepository.fetchCollectionBySharedCollectionId(sharedCollectionId = testSharedCollectionId).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Errata(errors) =>
              errors.length should beGreaterThanOrEqualTo(1)
          }
        }
    }

    "fetchCollectionByPosition" should {

      "return a Collection object when a existing position is given" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.fetchCollectionByPosition(position = testPosition).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Answer(maybeCollection) =>
              maybeCollection must beSome[Collection].which { collection =>
                collection.id shouldEqual testCollectionId
                collection.data.position shouldEqual testPosition
              }
          }
        }

      "return None when a non-existing position is given" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {
          val result = collectionRepository.fetchCollectionByPosition(position = testNonExistingPosition).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Answer(maybeCollection) =>
              maybeCollection must beNone
          }
        }

      "return a NineCardsException when a exception is thrown" in
        new CollectionRepositoryScope
          with ErrorCollectionRepositoryResponses {

          val result = collectionRepository.fetchCollectionByPosition(position = testPosition).run.run

          result must beLike[Result[Option[Collection], RepositoryException]] {
            case Errata(errors) =>
              errors.length should beGreaterThanOrEqualTo(1)
          }
        }
    }

    "fetchSortedCollections" should {

      "return all the cache categories stored in the database" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.fetchSortedCollections.run.run

          result must beLike[Result[Seq[Collection], RepositoryException]] {
            case Answer(collections) =>
              collections shouldEqual collectionSeq
          }
        }

      "return a NineCardsException when a exception is thrown" in
        new CollectionRepositoryScope
          with ErrorCollectionRepositoryResponses {

          val result = collectionRepository.fetchSortedCollections.run.run

          result must beLike[Result[Seq[Collection], RepositoryException]] {
            case Errata(errors) =>
              errors.length should beGreaterThanOrEqualTo(1)
          }
        }
    }

    "updateCollection" should {

      "return a successful result when the collection is updated" in
        new CollectionRepositoryScope
          with ValidCollectionRepositoryResponses {

          val result = collectionRepository.updateCollection(collection = collection).run.run

          result must beLike[Result[Int, RepositoryException]] {
            case Answer(updated) =>
              updated shouldEqual 1
          }
        }

      "return a NineCardsException when a exception is thrown" in
        new CollectionRepositoryScope
          with ErrorCollectionRepositoryResponses {

          val result = collectionRepository.updateCollection(collection = collection).run.run

          result must beLike[Result[Int, RepositoryException]] {
            case Errata(errors) =>
              errors.length should beGreaterThanOrEqualTo(1)
          }
        }
    }

    "getEntityFromCursor" should {

      "return None when an empty cursor is given" in
        new EmptyCollectionMockCursor
          with Scope {

          val result = getEntityFromCursor(collectionEntityFromCursor)(mockCursor)

          result must beNone
        }

      "return a Collection object when a cursor with data is given" in
        new CollectionMockCursor
          with Scope {

          val result = getEntityFromCursor(collectionEntityFromCursor)(mockCursor)

          result must beSome[CollectionEntity].which { collection =>
            collection.id shouldEqual collectionEntity.id
            collection.data shouldEqual collectionEntity.data
          }
        }
    }

    "getListFromCursor" should {

      "return an empty sequence when an empty cursor is given" in
        new EmptyCollectionMockCursor
          with Scope {

          val result = getListFromCursor(collectionEntityFromCursor)(mockCursor)

          result should beEmpty
        }

      "return a Collection sequence when a cursor with data is given" in
        new CollectionMockCursor
          with Scope {

          val result = getListFromCursor(collectionEntityFromCursor)(mockCursor)

          result shouldEqual collectionEntitySeq
        }
    }
  }
}
