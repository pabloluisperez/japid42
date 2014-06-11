/**
 * 
 */
package cn.bran.play.routing;

public  class  MyTuple2 <A, B>{
    private  A a;
    private B b;

    public MyTuple2(A _a, B _b) {
        this.a = _a;
        this.b = _b;
    }

    public A _1() {
        return this.a;

    }

    public B _2() {
        return this.b;
    }
}
