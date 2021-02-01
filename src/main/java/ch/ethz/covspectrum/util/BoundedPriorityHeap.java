package ch.ethz.covspectrum.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class BoundedPriorityHeap<E> {

    private final PriorityQueue<E> internal;

    private final int maxSize;


    public BoundedPriorityHeap(int maxSize) {
        internal = new PriorityQueue<>();
        this.maxSize = maxSize;
    }


    public BoundedPriorityHeap(int maxSize, Comparator<? super E> comparator) {
        internal = new PriorityQueue<>(comparator);
        this.maxSize = maxSize;
    }


    public boolean add(E el) {
        if (internal.size() < maxSize) {
            internal.add(el);
            return true;
        } else {
            internal.add(el);
            return internal.poll() == el;
        }
    }


    public E peek() {
        return internal.peek();
    }


    public E poll() {
        return internal.poll();
    }


    public int size() {
        return internal.size();
    }


    public boolean isFull() {
        return internal.size() == maxSize;
    }


    public List<E> getSortedList() {
        List<E> result = new ArrayList<>();
        while(!internal.isEmpty()){
            result.add(0, internal.poll());
        }
        return result;
    }
}
