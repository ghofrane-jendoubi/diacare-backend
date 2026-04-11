package tn.esprit.spring.diacarebackend.dto;

import lombok.Data;

@Data
public class DiabetesPredictionRequest {
    private int Age;
    private int Gender;           // 1 = Male, 0 = Female
    private int Polyuria;         // 1 = Yes, 0 = No
    private int Polydipsia;       // 1 = Yes, 0 = No
    private int sudden_weight_loss;
    private int weakness;
    private int Polyphagia;
    private int Genital_thrush;
    private int visual_blurring;
    private int Itching;
    private int Irritability;
    private int delayed_healing;
    private int partial_paresis;
    private int muscle_stiffness;
    private int Alopecia;
    private int Obesity;
}