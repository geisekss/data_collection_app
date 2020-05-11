#include "SmartIDCore.h"

SmartIDCore::SmartIDCore(string fileNameWalking, string fileNameAuth) {
    LOGD("loading models");
    LOGD("walking model: %s", fileNameWalking.c_str());
    clfWalking = StatModel::load<Boost>(fileNameWalking);

    // load authentication model if it exists
    if (FILE *file = fopen(fileNameAuth.c_str(), "r")) {
        fclose(file);
        LOGD("authenticate model");
        clfAuth = StatModel::load<RTrees>(fileNameAuth);
    }

    retTuple.reserve(2);
    LOGD("finished");
}

float* SmartIDCore::authenticate(float *data_x, float *data_y, float *data_z) {

    if(clfAuth.empty())
        return tuple(NEED_CALIBRATION, -1);

    LOGD("testing walking/non walking");
    LOGD("prediction size: %d", predictions.size());

    Mat sample_x = Mat(1, WINDOW_SIZE, CV_32FC1, data_x);
    Mat sample_y = Mat(1, WINDOW_SIZE, CV_32FC1, data_y);
    Mat sample_z = Mat(1, WINDOW_SIZE, CV_32FC1, data_z);
    sample_z = sample_z - 9.8;
    LOGD("sample_x: %lf, %lf, %lf, %lf, %lf", sample_x.at<float>(0,0), sample_x.at<float>(0, 1), sample_x.at<float>(0, 2), sample_x.at<float>(0, 3), sample_x.at<float>(0, 4));
    LOGD("sample_y: %lf, %lf, %lf, %lf, %lf", sample_y.at<float>(0,0), sample_y.at<float>(0, 1), sample_y.at<float>(0, 2), sample_y.at<float>(0, 3), sample_y.at<float>(0, 4));
    LOGD("sample_z: %lf, %lf, %lf, %lf, %lf", sample_z.at<float>(0,0), sample_z.at<float>(0, 1), sample_z.at<float>(0, 2), sample_z.at<float>(0, 3), sample_z.at<float>(0, 4));
    Scalar tempVal = mean( sample_z.row(0) );
    float myMAtMean = tempVal.val[0];
    LOGD("mean z: %lf", myMAtMean);

    Mat features;
    Mat features_x = getFeatureVector(sample_x);
    Mat features_y = getFeatureVector(sample_y);
    Mat features_z = getFeatureVector(sample_z);
    hconcat(features_x, features_y, features);
    hconcat(features, features_z, features);
    LOGD("features: %d x %d", features.rows, features.cols);

    float result = clfWalking->predict(features);
    LOGD("walking result: %f", result);

    if(result == 0)
        return tuple(NOT_WALKING, -1);

    float res = clfAuth->predict(features);
    predictions.push_back(res);

    if(predictions.size() == N_FRAMES) {
        int n_zeros = 0;
        int n_ones = 0;
        for(int k = 0; k < predictions.size(); k++) {
            if(predictions[k] == 1)
                n_ones++;
            else
                n_zeros++;
        }
        predictions.erase(predictions.begin());

        float tpr = ((float)n_ones / ((float)n_ones + (float)n_zeros)) * 100;
        float tnr = ((float)n_zeros / ((float)n_ones + (float)n_zeros)) * 100;

        if(n_ones > n_zeros)
            return tuple(AUTHENTICATED, tpr);
        else
            return tuple(NOT_AUTHENTICATED, tnr);
    }

    /*
    Mat probabilities;
    clfAuth->predict(features, probabilities, StatModel::RAW_OUTPUT);
    predictions.push_back(probabilities.at<float>(0,0));

    if(predictions.size() == N_FRAMES) {

        int n_zeros = 0;
        int n_ones = 0;
        int idx_max;
        float max_value = 0;
        for(int k = 0; k < predictions.size(); k++){
            if(abs(predictions[k]) > max_value){
                max_value = abs(predictions[k]);
                idx_max = k;
            }
            if(predictions[k] < 0)
                n_ones++;
            else
                n_zeros++;
        }

        float tpr = ((float)n_ones / ((float)n_ones + (float)n_zeros)) * 100;
        float tnr = ((float)n_zeros / ((float)n_ones + (float)n_zeros)) * 100;

        if(predictions[idx_max] < 0) {
            predictions.erase(predictions.begin());
            return tuple(AUTHENTICATED, tpr);
        } else {
            predictions.erase(predictions.begin());
            return tuple(NOT_AUTHENTICATED, tnr);
        }
    }
    */

    return tuple(LOADING_AUTH, -1);
}

void SmartIDCore::generateModel(string fileNamePos, string fileNameNeg, string fileNameModel) {
    LOGD("loading file positive");
    Ptr<TrainData> trainDataPositive = TrainData::loadFromCSV(fileNamePos, 0);
    Mat dataPositive = trainDataPositive->getTrainSamples();
    dataPositive.col(IDX_Z) = dataPositive.col(IDX_Z) - 9.8;

    LOGD("loading file negative");
    Ptr<TrainData> trainDataNegative = TrainData::loadFromCSV(fileNameNeg, 0);
    Mat dataNegative = trainDataNegative->getTrainSamples();

    LOGD("data positive: %d x %d", dataPositive.rows, dataPositive.cols);
    LOGD("data negative: %d x %d", dataNegative.rows, dataNegative.cols);
    LOGD("processing positive data");
    Mat featuresPositive = createFramesXYZ(N_EXAMPLES_POS, dataPositive);
    LOGD("processing negative data");
    Mat featuresNegative = createFramesXYZ((int)N_EXAMPLES_POS * RATE_NEG, dataNegative);
    LOGD("positive: %d; negative: %d", featuresPositive.rows, featuresNegative.rows);

    Mat labelsPositive = Mat::ones(featuresPositive.rows, 1, CV_32S);
    Mat labelsNegative = Mat::zeros(featuresNegative.rows, 1, CV_32S);

    LOGD("concatenating features");
    Mat features;
    features.push_back(featuresPositive);
    features.push_back(featuresNegative);

    Mat labels;
    labels.push_back(labelsPositive);
    labels.push_back(labelsNegative);

    LOGD("total features: %d, %d", features.rows, features.cols);
    LOGD("total labels: %d, %d", labels.rows, labels.cols);
    LOGD("training model");

    Ptr<TrainData> trainData = TrainData::create(features, ROW_SAMPLE, labels);

    clfAuth = RTrees::create();
    clfAuth->setTermCriteria(TermCriteria(TermCriteria::MAX_ITER, 1000, 1e-3));
    clfAuth->setMaxDepth(4);

    clfAuth->train(trainData);
    LOGD("saving model");
    clfAuth->save(fileNameModel);
    LOGD("finished");

}

Mat SmartIDCore::createFramesXYZ(int n_frames, Mat data){
    srand (time(NULL));

    Mat data_x = Mat(n_frames, WINDOW_SIZE, CV_32FC1);
    Mat data_y = Mat(n_frames, WINDOW_SIZE, CV_32FC1);
    Mat data_z = Mat(n_frames, WINDOW_SIZE, CV_32FC1);

    int n_examples = (int)(data.rows - WINDOW_SIZE)/STRIDE + 1;
    Mat visited = Mat::zeros(n_examples, 1, CV_32S);

    // create features using window size of 'size' and stride of 'stride
    LOGD("creating frames");
    LOGD("n frames: %d", n_frames);
    LOGD("n examples: %d", n_examples);
    int j = 0;
    while (j < n_frames){
        int choice = rand() % n_examples;
        //LOGD("choice: %d, visited? %d", choice, visited.at<int>(choice, 0));
        if(visited.at<int>(choice, 0) == 0){
            visited.at<int>(choice, 0) = 1;
            int i = choice * STRIDE;
            Mat aux = data.rowRange(i, i + WINDOW_SIZE);
            Mat aux_x = aux.col(IDX_X).t();
            aux_x.copyTo(data_x.row(j));
            Mat aux_y = aux.col(IDX_Y).t();
            aux_y.copyTo(data_y.row(j));
            Mat aux_z = aux.col(IDX_Z).t();
            aux_z.copyTo(data_z.row(j));
            //LOGD("i: %d", i);
            //LOGD("x: %lf, y: %lf, z: %lf", aux_x.at<float>(0, 0), aux_y.at<float>(0, 0), aux_z.at<float>(0, 0));
            j++;
        }
    }

    LOGD("x: %lf, %lf, %lf, %lf, %lf", data_x.at<float>(0,0), data_x.at<float>(0,1), data_x.at<float>(0,2), data_x.at<float>(0,3), data_x.at<float>(0,4));
    LOGD("y: %lf, %lf, %lf, %lf, %lf", data_y.at<float>(0,0), data_y.at<float>(0,1), data_y.at<float>(0,2), data_y.at<float>(0,3), data_y.at<float>(0,4));
    LOGD("z: %lf, %lf, %lf, %lf, %lf", data_z.at<float>(0,0), data_z.at<float>(0,1), data_z.at<float>(0,2), data_z.at<float>(0,3), data_z.at<float>(0,4));

    Scalar tempVal = mean(data_z.row(0));
    float myMAtMean = tempVal.val[0];
    LOGD("mean z 0: %lf", myMAtMean);
    tempVal = mean(data_z.row(1));
    myMAtMean = tempVal.val[0];
    LOGD("mean z 1: %lf", myMAtMean);


    LOGD("extracting features");
    Mat features;
    hconcat(getFeatureVector(data_x), getFeatureVector(data_y), features);
    hconcat(features, getFeatureVector(data_z), features);

    return features;
}

Mat SmartIDCore::shuffleMatrixRows(const Mat &matrix) {
    vector <int> seeds;
    for (int cont = 0; cont < matrix.rows; cont++)
        seeds.push_back(cont);

    randShuffle(seeds);

    Mat output;
    for (int cont = 0; cont < matrix.rows; cont++)
        output.push_back(matrix.row(seeds[cont]));

    return output;
}

float SmartIDCore::calculateEnergy(double *array, int n) {
    double res = 0.0;
    int i;

    for(i = 0; i < n; i++)
        res += array[i] * array[i];
    return res;
}

float SmartIDCore::calculateEntropy(double *array, int n){
    size_t k = 8;
    int i;

    gsl_histogram * h = gsl_histogram_alloc (k);
    float max = gsl_stats_max(array, 1, n);
    float min = gsl_stats_min(array, 1, n);

    gsl_histogram_set_ranges_uniform (h, min, max);
    for(i=0; i<n; i++){
        gsl_histogram_increment (h, array[i]);
    }

    float pmf_log[k];
    float pmf_sum = 0;
    double sum_h = gsl_histogram_sum(h);
    for(i=0; i<k; i++){
        double value_bin = *(h->bin+i);
        double pmf = value_bin/sum_h;

        if(pmf == 0){
           pmf_log[i] = 0;
        }
        else{
            pmf_log[i] = pmf * gsl_sf_log(pmf);
            pmf_sum += pmf * gsl_sf_log(pmf);
        }

    }

    gsl_histogram_free (h);

    return -pmf_sum;
}

void SmartIDCore::returnAbsVector(double *array, int n, double *new_array){

    for(int i=0; i<n; i++){
        new_array[i] = abs(array[i]);
    }
}

void SmartIDCore::clearPredictions(){
    predictions.clear();
}

Mat SmartIDCore::getFeatureVector(Mat data) {
    int i, m, j, k;
    gsl_wavelet *w;
    gsl_wavelet_workspace *work;

    Mat features(0, 0, CV_32FC1);
    int n_frames = data.rows;

    for(i = 0; i < n_frames; i++){

        double frame[WINDOW_SIZE];
        for(m = 0; m < WINDOW_SIZE; m++)
            frame[m] = data.at<float>(i, m);

        Mat_<float> statistics_wavelet;
        Mat_<float> arr_energy;
        Mat_<float> statistics_time;
        int final = 0;

        w = gsl_wavelet_alloc (gsl_wavelet_haar, 2);
        work = gsl_wavelet_workspace_alloc (WINDOW_SIZE);
        gsl_wavelet_transform_forward(w, frame, 1, WINDOW_SIZE, work);

        // TODO: ENTROPIA
        for(j = 1; j <= (log(WINDOW_SIZE) / log(2)); j++) {
            int len = (int) pow(2, j - 1);

            if(len > 2) {
                double coeff[len];
                final = len;

                for(k = 0; k < len; k++)
                    coeff[k] = frame[final + k];

                float energy_value = calculateEnergy(coeff, len);
                arr_energy.push_back(energy_value);

                float mean = gsl_stats_mean(coeff, 1, len);
                statistics_wavelet.push_back(mean);

                float std = gsl_stats_sd_m(coeff, 1, len, mean);
                statistics_wavelet.push_back(std);

                float skew = gsl_stats_skew_m_sd(coeff, 1, len, mean, std);
                statistics_wavelet.push_back(skew);

                float kurtosis = gsl_stats_kurtosis_m_sd(coeff, 1, len, mean, std);
                statistics_wavelet.push_back(kurtosis);

                 float entropy = calculateEntropy(coeff, len);
                 statistics_wavelet.push_back(entropy);
            }
        }

        float sum_energy = sum(arr_energy)[0];
        for(k = 0; k < arr_energy.rows; k++)
            arr_energy.at<float>(k) = arr_energy.at<float>(k) / sum_energy;

        float mean = gsl_stats_mean(frame, 1, WINDOW_SIZE);
        statistics_time.push_back(mean);

        float std = gsl_stats_sd_m(frame, 1, WINDOW_SIZE, mean);
        statistics_time.push_back(std);

        float skew = gsl_stats_skew_m_sd(frame, 1, WINDOW_SIZE, mean, std);
        statistics_time.push_back(skew);

        float kurtosis = gsl_stats_kurtosis_m_sd(frame, 1, WINDOW_SIZE, mean, std);
        statistics_time.push_back(kurtosis);

        float max = gsl_stats_max(frame, 1, WINDOW_SIZE);
        statistics_time.push_back(max);

        float min = gsl_stats_min(frame, 1, WINDOW_SIZE);
        statistics_time.push_back(min);

        double abs_vector[WINDOW_SIZE];
        returnAbsVector(frame, WINDOW_SIZE, abs_vector);

        float intensity = gsl_stats_mean(abs_vector, 1, WINDOW_SIZE);
        statistics_time.push_back(intensity);

        Mat_<float> aux;
        aux.push_back(statistics_wavelet);
        aux.push_back(arr_energy);
        aux.push_back(statistics_time);

        Mat_<float> trans;
        transpose(aux, trans);
        features.push_back(trans);
    }

    return features;
}

float * SmartIDCore::tuple(int result, float confidence) {
    retTuple[0] = result;
    retTuple[1] = confidence;
    return &retTuple[0];
}