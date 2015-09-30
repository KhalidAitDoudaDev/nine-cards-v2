package com.fortysevendeg.ninecardslauncher.repository.repositories

import com.fortysevendeg.ninecardslauncher.commons.NineCardExtensions._
import com.fortysevendeg.ninecardslauncher.commons.contentresolver.Conversions._
import com.fortysevendeg.ninecardslauncher.commons.contentresolver.{ContentResolverWrapper, UriCreator}
import com.fortysevendeg.ninecardslauncher.commons.services.Service
import com.fortysevendeg.ninecardslauncher.commons.services.Service._
import com.fortysevendeg.ninecardslauncher.repository.Conversions.toGeoInfo
import com.fortysevendeg.ninecardslauncher.repository.model.{GeoInfo, GeoInfoData}
import com.fortysevendeg.ninecardslauncher.repository.provider.GeoInfoEntity
import com.fortysevendeg.ninecardslauncher.repository.provider.GeoInfoEntity._
import com.fortysevendeg.ninecardslauncher.repository.provider.NineCardsUri._
import com.fortysevendeg.ninecardslauncher.repository.{ImplicitsRepositoryExceptions, RepositoryException}

import scalaz.concurrent.Task

class GeoInfoRepository(
  contentResolverWrapper: ContentResolverWrapper,
  uriCreator: UriCreator)
  extends ImplicitsRepositoryExceptions {

  val geoInfoUri = uriCreator.parse(geoInfoUriString)

  def addGeoInfo(data: GeoInfoData): ServiceDef2[GeoInfo, RepositoryException] =
    Service {
      Task {
        CatchAll[RepositoryException] {
          val values = Map[String, Any](
            constrain -> data.constrain,
            occurrence -> data.occurrence,
            wifi -> data.wifi,
            latitude -> data.latitude,
            longitude -> data.longitude,
            system -> data.system)

          val id = contentResolverWrapper.insert(
            uri = geoInfoUri,
            values = values)

          GeoInfo(id = id, data = data)
        }
      }
    }

  def deleteGeoInfo(geoInfo: GeoInfo): ServiceDef2[Int, RepositoryException] =
    Service {
      Task {
        CatchAll[RepositoryException] {
          contentResolverWrapper.deleteById(
            uri = geoInfoUri,
            id = geoInfo.id)
        }
      }
    }

  def fetchGeoInfoItems: ServiceDef2[Seq[GeoInfo], RepositoryException] =
    Service {
      Task {
        CatchAll[RepositoryException] {
          contentResolverWrapper.fetchAll(
            uri = geoInfoUri,
            projection = allFields)(getListFromCursor(geoInfoEntityFromCursor)) map toGeoInfo
        }
      }
    }

  def findGeoInfoById(id: Int): ServiceDef2[Option[GeoInfo], RepositoryException] =
    Service {
      Task {
        CatchAll[RepositoryException] {
          contentResolverWrapper.findById(
            uri = geoInfoUri,
            id = id,
            projection = allFields)(getEntityFromCursor(geoInfoEntityFromCursor)) map toGeoInfo
        }
      }
    }


  def fetchGeoInfoByConstrain(constrain: String): ServiceDef2[Option[GeoInfo], RepositoryException] =
    Service {
      Task {
        CatchAll[RepositoryException] {
          contentResolverWrapper.fetch(
            uri = geoInfoUri,
            projection = allFields,
            where = s"${GeoInfoEntity.constrain} = ?",
            whereParams = Array(constrain))(getEntityFromCursor(geoInfoEntityFromCursor)) map toGeoInfo
        }
      }
    }

  def updateGeoInfo(geoInfo: GeoInfo): ServiceDef2[Int, RepositoryException] =
    Service {
      Task {
        CatchAll[RepositoryException] {
          val values = Map[String, Any](
            constrain -> geoInfo.data.constrain,
            occurrence -> geoInfo.data.occurrence,
            wifi -> geoInfo.data.wifi,
            latitude -> geoInfo.data.latitude,
            longitude -> geoInfo.data.longitude,
            system -> geoInfo.data.system)

          contentResolverWrapper.updateById(
            uri = geoInfoUri,
            id = geoInfo.id,
            values = values)
        }
      }
    }
}