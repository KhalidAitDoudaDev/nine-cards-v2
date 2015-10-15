package com.fortysevendeg.ninecardslauncher.services.persistence.models

case class App(
  id: Int,
  name: String,
  packageName: String,
  className: String,
  category: String,
  imagePath: String,
  colorPrimary: String,
  dateInstalled: Double,
  dateUpdate: Double,
  version: String,
  installedFromGooglePlay: Boolean)

case class Collection(
  id: Int,
  position: Int,
  name: String,
  collectionType: String,
  icon: String,
  themedColorIndex: Int,
  appsCategory: Option[String] = None,
  constrains: Option[String] = None,
  originalSharedCollectionId: Option[String] = None,
  sharedCollectionId: Option[String] = None,
  sharedCollectionSubscribed: Boolean,
  cards: Seq[Card] = Seq.empty)

case class Card(
  id: Int,
  position: Int,
  micros: Int = 0,
  term: String,
  packageName: Option[String],
  cardType: String,
  intent: String,
  imagePath: String,
  starRating: Option[Double] = None,
  numDownloads: Option[String] = None,
  notification: Option[String] = None)

case class GeoInfo(
  id: Int,
  constrain: String,
  occurrence: String,
  wifi: String,
  latitude: Double,
  longitude: Double,
  system: Boolean)
