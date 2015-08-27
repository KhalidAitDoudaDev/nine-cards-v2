package com.fortysevendeg.ninecardslauncher.commons.contentresolver

import android.net.Uri

class UriCreator {

  def parse(uriString: String): Uri = Uri.parse(uriString)
  
  def withAppendedPath(uri: Uri, path: String) = Uri.withAppendedPath(uri, Uri.encode(path))

}
