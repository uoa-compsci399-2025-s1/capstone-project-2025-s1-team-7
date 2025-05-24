package com.example.compsci399testproject.machinelearning.models;

public abstract class ClassifierModel {
    public abstract double[] score(float[] input);
}
