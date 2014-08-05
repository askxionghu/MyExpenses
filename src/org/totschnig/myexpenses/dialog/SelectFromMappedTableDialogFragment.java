/*   This file is part of My Expenses.
 *   My Expenses is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   My Expenses is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with My Expenses.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.totschnig.myexpenses.dialog;

import org.totschnig.myexpenses.activity.MyExpenses;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ACCOUNTID;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_LABEL;
import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;

import org.totschnig.myexpenses.provider.filter.IdCriteria;
import org.totschnig.myexpenses.ui.SimpleCursorAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

public abstract class SelectFromMappedTableDialogFragment extends CommitSafeDialogFragment implements OnClickListener,
    LoaderManager.LoaderCallbacks<Cursor>
{
  protected SimpleCursorAdapter mAdapter;
  protected Cursor mCursor;
  
  abstract int getDialogTitle();
  abstract int getCriteriaTitle();
  abstract int getCommand();
  abstract String getColumn();
  abstract Uri getUri();
  
  /**
   * needed by PaymentMethod to translate labels of default methods
   * @param label
   * @return
   */
  protected String getDisplayLabel(String label) {
    return label;
  }
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Context wrappedCtx = DialogUtils.wrapContext1(getActivity());
    mAdapter = new SimpleCursorAdapter(wrappedCtx, android.R.layout.simple_list_item_single_choice, null,
        new String[] {KEY_LABEL}, new int[] {android.R.id.text1}, 0) {
      @Override
      public void setViewText(TextView v, String text) {
        super.setViewText(v, getDisplayLabel(text));
      }
    };
    getLoaderManager().initLoader(0, null, this);
    return new AlertDialog.Builder(wrappedCtx)
      .setTitle(getDialogTitle())
      .setSingleChoiceItems(mAdapter, -1, this)
      .create();
  }
  @Override
  public void onClick(DialogInterface dialog, int which) {
    if (getActivity()==null || mCursor == null) {
      return;
    }
    mCursor.moveToPosition(which);
    ((MyExpenses) getActivity()).addFilterCriteria(
        getCommand(),
        new IdCriteria(getString(getCriteriaTitle()),
            getColumn(),
            mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID)),
            getDisplayLabel(mCursor.getString(mCursor.getColumnIndex(KEY_LABEL)))));
    dismiss();
  }
  @Override
  public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
    if (getActivity()==null) {
      return null;
    }
    CursorLoader cursorLoader = new CursorLoader(
        getActivity(),
        getUri(),
        null,
        KEY_ACCOUNTID + " = ?",
        new String[] {String.valueOf(getArguments().getLong(KEY_ACCOUNTID))},
        null);
    return cursorLoader;

  }
  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
    mCursor = data;
    mAdapter.swapCursor(data);
  }
  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    mCursor = null;
    mAdapter.swapCursor(null);
  }
}