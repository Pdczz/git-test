package com.leyou;

public class Student {
    private String stuno;
    private String name;
    private float math;
    private float english;
    private float computer;
    public Student() {
    }
    public Student(String stuno, String name, float math, float english, float computer) {
        this.stuno = stuno;
        this.name = name;
        this.math = math;
        this.english = english;
        this.computer = computer;
    }
    public String getStuno() {
        return stuno;
    }
    public void setStuno(String stuno) {
        this.stuno = stuno;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public float getMath() {
        return math;
    }
    public void setMath(float math) {
        this.math = math;
    }
    public float getEnglish() {
        return english;
    }
    public void setEnglish(float english) {
        this.english = english;
    }
    public float getComputer() {
        return computer;
    }
    public void setComputer(float computer) {
        this.computer = computer;
    }
    public float sum(){
        return math+english+computer;
    }
    public float avg(){
        return sum()/3;
    }
    public float max(){
        return math>english?(math>computer?math:computer):(english>computer?english:computer);
    }
    public float min(){
        return math<english?(math<computer?math:computer):(english<computer?english:computer);
    }
}
