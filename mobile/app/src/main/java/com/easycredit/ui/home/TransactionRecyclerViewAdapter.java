package com.easycredit.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.easycredit.R;
import com.easycredit.data.model.UserTransaction;

import java.util.Date;
import java.util.List;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_NO_YEAR;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.formatDateTime;
import static android.text.format.DateUtils.isToday;

/**
 * {@link RecyclerView.Adapter} that can display a {@link UserTransaction}.
 */
public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "TransactionRecyclerView";
    private static final String RUPEE_SYMBOL = "₹";

    private final List<UserTransaction> mTransactions;
    private Context context;

    public TransactionRecyclerViewAdapter(Context ctx, List<UserTransaction> items) {
        mTransactions = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_transaction, parent, false);
        context = parent.getContext().getApplicationContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");

        int amount = mTransactions.get(position).getAmount();
        int red = 0xFFEB683F;
        int green = 0xFF78DA95;
        String beneficiaryName = mTransactions.get(position).getBeneficiaryName();
        String status = mTransactions.get(position).getStatus().name();
        Date timestamp = mTransactions.get(position).getTimestamp();
        String formattedDate = formatDateTime(context, timestamp.getTime(),
                FORMAT_ABBREV_MONTH | FORMAT_NO_YEAR);
        String formattedTime = formatDateTime(context, timestamp.getTime(), FORMAT_SHOW_TIME);
        String timeString = isToday(timestamp.getTime()) ? formattedTime + " today" : formattedDate;

        holder.amount.setTextColor(amount < 0 ? red : green);

        holder.beneficiaryName.setText(beneficiaryName);
        holder.amount.setText(putRupeeSymbol(amount));
        holder.timestamp.setText(timeString);
        holder.status.setText(status);
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    private String putRupeeSymbol(int amount)
    {
        if (amount > -1)
        {
            return String.format("%s%s", RUPEE_SYMBOL, amount);
        }
        else
        {
            return String.format("- %s%s", RUPEE_SYMBOL, Math.abs(amount));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private TextView beneficiaryName;
        private TextView timestamp;
        private TextView amount;
        private TextView status;
        private LinearLayout transactionListItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            beneficiaryName = view.findViewById(R.id.beneficiaryName);
            timestamp = view.findViewById(R.id.timestamp);
            amount = view.findViewById(R.id.amount);
            status = view.findViewById(R.id.status);
            transactionListItem = view.findViewById(R.id.transactionListItem);
        }
    }
}
