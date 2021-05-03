package com.flytbase.madscalculator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flytbase.madscalculator.R;
import com.flytbase.madscalculator.model.CalculationHistory;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final ArrayList<CalculationHistory> calculationList;
    private final OnItemInteractionListener interactionListener;
    public Context mContext;

    public HistoryAdapter(ArrayList<CalculationHistory> calculationList, Context context,
                          OnItemInteractionListener interactionListener) {
        this.mContext = context;
        this.calculationList = calculationList;
        this.interactionListener = interactionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_calculation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalculationHistory calculationHistory = calculationList.get(position);
        holder.txtExpression.setText(String.format("Expression: %s", calculationHistory.getExpression()));
        holder.txtResult.setText(String.format("Result: %s", calculationHistory.getAnswer()));
        holder.layoutInput.setOnClickListener(v -> {
            if (interactionListener != null) {
                interactionListener.onItemClick(calculationHistory.getExpression());
            }
        });
    }

    @Override
    public int getItemCount() {
        return calculationList.size();
    }

    public interface OnItemInteractionListener {
        void onItemClick(String expression);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout layoutInput;
        private final TextView txtExpression;
        private final TextView txtResult;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutInput = itemView.findViewById(R.id.layoutInput);
            txtExpression = itemView.findViewById(R.id.tvQuestion);
            txtResult = itemView.findViewById(R.id.tvAnswer);
        }

    }
}