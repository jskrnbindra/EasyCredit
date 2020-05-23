package com.easycredit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.easycredit.data.model.UserTransaction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} that can display a {@link UserTransaction}..
 */
public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "TransactionRecyclerView";

    private final List<UserTransaction> mTransactions;
    private Context context;

    public TransactionRecyclerViewAdapter(Context ctx, List<UserTransaction> items) {
        mTransactions = items;
        context = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: called");

        holder.beneficiaryName.setText(mTransactions.get(position).getBeneficiary().getDisplayName());
        holder.amount.setText(""+mTransactions.get(position).getAmount());
        SimpleDateFormat format = new SimpleDateFormat("MMM d", Locale.US);
        holder.timestamp.setText(format.format(mTransactions.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        private TextView beneficiaryName;
        private TextView timestamp;
        private TextView amount;
        private LinearLayout transactionListItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            beneficiaryName = view.findViewById(R.id.beneficiaryName);
            timestamp = view.findViewById(R.id.timestamp);
            amount = view.findViewById(R.id.amount);
            transactionListItem = view.findViewById(R.id.transactionListItem);
        }
    }
}
