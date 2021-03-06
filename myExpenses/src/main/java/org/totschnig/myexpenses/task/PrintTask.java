package org.totschnig.myexpenses.task;

import static org.totschnig.myexpenses.provider.DatabaseConstants.KEY_ROWID;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.fragment.TransactionList;
import org.totschnig.myexpenses.model.Account;
import org.totschnig.myexpenses.provider.filter.WhereFilter;
import org.totschnig.myexpenses.util.Result;
import org.totschnig.myexpenses.util.Utils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

public class PrintTask extends AsyncTask<Void, String, Result> {
  private final TaskExecutionFragment taskExecutionFragment;
  private long accountId;
  private WhereFilter filter;

  public PrintTask(TaskExecutionFragment taskExecutionFragment, Bundle extras) {
    this.taskExecutionFragment = taskExecutionFragment;
    accountId = extras.getLong(KEY_ROWID);
    filter = new WhereFilter(extras.getSparseParcelableArray(TransactionList.KEY_FILTER));
  }

  /*
   * (non-Javadoc) reports on success triggering restart if needed
   * 
   * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
   */
  @Override
  protected void onPostExecute(Result result) {
    if (this.taskExecutionFragment.mCallbacks != null) {
      this.taskExecutionFragment.mCallbacks.onPostExecute(TaskExecutionFragment.TASK_PRINT, result);
    }
  }

  /* (non-Javadoc)
   * this is where the bulk of the work is done via calls to {@link #importCatsMain()}
   * and {@link #importCatsSub()}
   * sets up {@link #categories} and {@link #sub_categories}
   * @see android.os.AsyncTask#doInBackground(Params[])
   */
  @Override
  protected Result doInBackground(Void... ignored) {
    Account account;
    DocumentFile appDir = Utils.getAppDir();
    if (appDir == null) {
      return new Result(false,R.string.external_storage_unavailable);
    }
    account = Account.getInstanceFromDb(accountId);
    try {
      return account.print(appDir,filter);
    } catch (Exception e) {
      Log.e("DEBUG","Error while printing",e);
      return new Result(false,
          R.string.export_sdcard_failure,
          appDir.getName(),e.getMessage());
    }
  }
}
