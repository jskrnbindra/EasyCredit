package com.easycredit.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.easycredit.R;
import com.easycredit.data.enums.TransactionStatus;
import com.easycredit.data.model.EasyCreditUser;
import com.easycredit.data.model.UserTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class TransactionsFragment extends Fragment implements HomeActivity.RefreshButtonListener {

    private static final String TAG = "TransactionsFragment";

    private List<UserTransaction> transactions = new ArrayList<>();
    private TransactionRecyclerViewAdapter transactionAdapter;


    public TransactionsFragment() {
        HomeActivity.setRefreshButtonListener(this);
    }

    public static TransactionsFragment newInstance() {
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        populateTransactions();
        Context context = view.getContext();
        transactionAdapter = new TransactionRecyclerViewAdapter(context, transactions);
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(transactionAdapter);
        }
        return view;
    }

    private void populateTransactions()
    {
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "HezHabibi@gmail.com",
                "Hez Habibi", "9988998899"), 499, new Date(), TransactionStatus.DONE));

        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.PENDING));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.DONE));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.EXPIRED));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.DONE));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.PENDING));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.DONE));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.EXPIRED));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.DONE));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.PENDING));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.DONE));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.EXPIRED));
        transactions.add(new UserTransaction(new EasyCreditUser("some-id",
                "begumrubina@gmail.com",
                "B Rubina", "9988775599"), -200, new Date(), TransactionStatus.DONE));
    }

    @Override
    public void refreshButtonClicked() {
        Toast.makeText(getActivity(),"Refresh button clicked", Toast.LENGTH_LONG).show();
        transactions.add(0, new UserTransaction(new EasyCreditUser("blah-blah-blah",
                "blabla@gmail.com",
                "Bla bla", "9988"), -200, new Date(), TransactionStatus.DONE));
        transactionAdapter.notifyDataSetChanged();

    }
}
