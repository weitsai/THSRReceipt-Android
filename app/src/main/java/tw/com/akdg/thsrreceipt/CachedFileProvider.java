package tw.com.akdg.thsrreceipt;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;


public class CachedFileProvider extends ContentProvider {

  private static final String AUTHORITY = "tw.com.akdg.thsrreceipt.provider";

  private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    mUriMatcher.addURI(AUTHORITY, "*", 1);
  }

  public static String getAuthority() { return AUTHORITY; }

  @Override
  public boolean onCreate() { return true; }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }

  @Override
  public String getType(Uri uri) { return null; }

  @Override
  public Uri insert(Uri uri, ContentValues values) { return null; }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    switch (mUriMatcher.match(uri)) {
      case 1 :
        String fileLocation = getContext().getCacheDir() + "/" + uri.getLastPathSegment();
        Log.v("fileLocation = ", fileLocation);
        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(
                new File(fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
        return parcelFileDescriptor;
      default:
        throw new FileNotFoundException("ParcelFileDescriptor openFile is Error " + uri.toString());
    }
  }
}
