
#ifndef SMARTID_CORE_H
#define SMARTID_CORE_H

#include <android/log.h>
#include <iostream>
#include <fstream>
#include <iterator>
#include <vector>
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/ml/ml.hpp>
#include <gsl/gsl_statistics.h>
#include <gsl/gsl_wavelet.h>
#include <gsl/gsl_histogram.h>
#include <gsl/gsl_sf_log.h>

#define LOG_TAG    "SMARTID_NATIVE"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

#define STRIDE 64
#define WINDOW_SIZE 256
#define RATE_NEG 1.2
#define N_EXAMPLES_POS 150
#define N_FRAMES 9

#define LOADING_AUTH -2
#define NEED_CALIBRATION -1
#define NOT_WALKING 0
#define NOT_AUTHENTICATED 1
#define AUTHENTICATED 2

#define IDX_X 1
#define IDX_Y 2
#define IDX_Z 3

#define EPSILON 0.0000001

using namespace std;
using namespace cv;
using namespace ml;

class SmartIDCore {

public:
    SmartIDCore(string fileNameWalking, string fileNameAuth);
    void generateModel(string fileNamePos, string fileNameNeg, string fileNameModel);
    float* authenticate(float *data_x, float *data_y, float *data_z);
    void clearPredictions();

private:
    Ptr<Boost> clfWalking;
    Ptr<RTrees> clfAuth;
    vector<float> predictions;
    vector<float> retTuple;

    float calculateEnergy(double *array, int n);
    float calculateEntropy(double *array, int n);
    Mat getFeatureVector(Mat data);
    Mat shuffleMatrixRows(const Mat &matrix);
    float * tuple(int result, float confidence);
    Mat createFramesXYZ(int n_frames, Mat data);
    void returnAbsVector(double *array, int n, double *new_array);

};


#endif //SMARTID_CORE_H
