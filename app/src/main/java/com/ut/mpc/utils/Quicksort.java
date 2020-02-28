/*
 * Adapted from: http://www.vogella.com/tutorials/JavaAlgorithmsQuicksort/article.html
 *
 */

package com.ut.mpc.utils;
import java.util.List;

public class Quicksort  {
    private List<STPoint> numbers;
    private List<Double> numbersDub;
    private int number;
    private int low;
    private int high;

    public void sort(List<Double> values, int low, int high){
        // check for empty or null array
        if (values == null || values.size() == 0){
            return;
        }
        this.numbersDub = values;
        this.number = values.size();
        quicksort(low, high);
    }

    private void quicksort(int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        double pivot = this.numbersDub.get((low + (high-low)/2));

        // Divide into two lists
        while (i <= j) {
            // If the current value from the left list is smaller then the pivot
            // element then get the next element from the left list
            while (this.numbersDub.get(i).doubleValue() < pivot) {
                i++;
            }
            // If the current value from the right list is larger then the pivot
            // element then get the next element from the right list
            while (this.numbersDub.get(j).doubleValue() > pivot) {
                j--;
            }

            // If we have found a values in the left list which is larger then
            // the pivot element and if we have found a value in the right list
            // which is smaller then the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchangeDub(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort(low, j);
        if (i < high)
            quicksort(i, high);
    }

    private void exchangeDub(int i, int j) {
        Double temp = this.numbersDub.get(i);
        this.numbersDub.set(i, this.numbersDub.get(j));
        this.numbersDub.set(j, temp);
    }

    public static void sort(List<STPoint> values, int low, int high, int dim){
        Quicksort qs = new Quicksort();
        // check for empty or null array
        if (values == null || values.size() == 0){
            return;
        }
        qs.numbers = values;
        qs.number = values.size();
        qs.quicksort(low, high, dim);
    }

    private double getCoordinate(int index, int dim){
        if(dim == 0){
            return this.numbers.get(index).getX();
        } else if(dim == 1) {
            return this.numbers.get(index).getY();
        } else if(dim == 2) {
            return this.numbers.get(index).getT();
        } else{
            throw new RuntimeException("Unexpected dimension values : > " + dim);
        }
    }

    private void quicksort(int low, int high, int dim) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        double pivot = this.getCoordinate((low + (high-low)/2), dim);

        // Divide into two lists
        while (i <= j) {
            // If the current value from the left list is smaller then the pivot
            // element then get the next element from the left list
            while (this.getCoordinate(i,dim) < pivot) {
                i++;
            }
            // If the current value from the right list is larger then the pivot
            // element then get the next element from the right list
            while (this.getCoordinate(j,dim) > pivot) {
                j--;
            }

            // If we have found a values in the left list which is larger then
            // the pivot element and if we have found a value in the right list
            // which is smaller then the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort(low, j, dim);
        if (i < high)
            quicksort(i, high, dim);
    }

    private void exchange(int i, int j) {
        STPoint temp = this.numbers.get(i);
        this.numbers.set(i, this.numbers.get(j));
        this.numbers.set(j, temp);
    }

}
