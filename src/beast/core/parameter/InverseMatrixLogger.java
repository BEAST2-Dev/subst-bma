package beast.core.parameter;

import beast.core.Input;
import beast.core.Loggable;
import beast.core.Plugin;
import beast.math.matrixAlgebra.Matrix;

import java.io.PrintStream;

/**
 * @author Chieh-Hsi Wu
 */
public class InverseMatrixLogger extends Plugin implements Loggable {
    public Input<RealParameter> parameterInput = new Input<RealParameter>("parameter","The matrix as a vector.", Input.Validate.REQUIRED);

    RealParameter parameter;
    public void initAndValidate(){
        parameter = parameterInput.get();
    }

    public void init(PrintStream out){
        String prefix = "inverse("+parameter.getID()+").";
        for(int i = 0; i < parameter.getDimension();i++){
            out.print(prefix+i+"\t");
        }
    }


    public void log(int nSample, PrintStream out){
        int dim = (int)Math.sqrt(parameter.getDimension());
        double[][] mat = new double[dim][dim];
        int k = 0;
        for(int i = 0; i < mat.length;i++){
            for(int j = 0; j < mat[i].length;j++){
                mat[i][j] = parameter.getValue(k++);
            }
        }
        Matrix matrix = new Matrix(mat);
        double[][] invMat = matrix.inverse().toComponents();
        k= 0;
        for(int i = 0; i < invMat.length;i++){
            for(int j = 0; j < invMat[i].length; j++){
                out.print(invMat[i][j]+"\t");
            }
        }
    }

    public void close(PrintStream out){

    }
}