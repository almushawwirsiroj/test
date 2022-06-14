package com.androidtutorial.mybookapp.filters;

import android.widget.Filter;

import com.androidtutorial.mybookapp.adapters.AdapterCategory;
import com.androidtutorial.mybookapp.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    //array list in whick we want to search
    ArrayList<ModelCategory> filterList;
    //adapter in which filter need to ve implemented
    AdapterCategory adapterCategory;

    //constructor
    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should no be null and empty
        //nilai tidak boleh nol dan kosong
        if (constraint != null && constraint.length() > 0){
            //change to uppper case, or lower case to avoid case sensitivy
            //ubah ke huruf besar, atau huruf kecil untuk menghindari kepekaan huruf besar
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //validasi
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    //add to filteredlist
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results; //dont miss is
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>) results.values;

         //notify changes
        adapterCategory.notifyDataSetChanged();

    }
}
