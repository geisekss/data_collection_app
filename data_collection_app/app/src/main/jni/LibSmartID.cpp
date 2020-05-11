#include <jni.h>

#include "SmartIDCore.h"

extern "C" {

    JNIEXPORT jlong JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeCreateObject(JNIEnv *jnienv, jobject object, jstring fileNameWalking, jstring fileNameAuth);
    JNIEXPORT jlong JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeCreateObject(JNIEnv *jnienv, jobject object, jstring fileNameWalking, jstring fileNameAuth) {
        LOGD("nativeCreateObject enter");

        const char *jnamestr1 = jnienv->GetStringUTFChars(fileNameWalking, NULL);
        string modelWalkingFileName(jnamestr1);

        const char *jnamestr2 = jnienv->GetStringUTFChars(fileNameAuth, NULL);
        string modelAuthFileName(jnamestr2);

        jlong result = 0;
        result = (jlong) new SmartIDCore(modelWalkingFileName, modelAuthFileName);
        return result;
    }


    JNIEXPORT void JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeGenerateModel(JNIEnv *jnienv, jobject object, jlong thiz, jstring fileNamePos, jstring fileNameNeg, jstring fileNameModel);
    JNIEXPORT void JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeGenerateModel(JNIEnv *jnienv, jobject object, jlong thiz, jstring fileNamePos, jstring fileNameNeg, jstring fileNameModel) {
        LOGD("nativeGenerateModel enter");

        const char *jstringPositive = jnienv->GetStringUTFChars(fileNamePos, NULL);
        string positive(jstringPositive);

        const char *jstringNegative = jnienv->GetStringUTFChars(fileNameNeg, NULL);
        string negative(jstringNegative);

        const char *jstringModel = jnienv->GetStringUTFChars(fileNameModel, NULL);
        string model(jstringModel);

        ((SmartIDCore*)thiz)->generateModel(positive, negative, model);
    }

    JNIEXPORT jfloatArray JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeAuthenticate(JNIEnv *jnienv, jobject object, jlong thiz, jfloatArray arr_x, jfloatArray arr_y, jfloatArray arr_z);
    JNIEXPORT jfloatArray JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeAuthenticate(JNIEnv *jnienv, jobject object, jlong thiz, jfloatArray arr_x, jfloatArray arr_y, jfloatArray arr_z) {
        jsize len = jnienv->GetArrayLength(arr_x);
        float data_x[len];
        float data_y[len];
        float data_z[len];

        jfloat* tab_x = jnienv->GetFloatArrayElements(arr_x, 0);
        for (int i = 0; i < len ; i++)
            data_x[i] = tab_x[i];

        jfloat* tab_y = jnienv->GetFloatArrayElements(arr_y, 0);
        for (int i = 0; i < len ; i++)
            data_y[i] = tab_y[i];

        jfloat* tab_z = jnienv->GetFloatArrayElements(arr_z, 0);
        for (int i = 0; i < len ; i++)
            data_z[i] = tab_z[i];

        jnienv->ReleaseFloatArrayElements(arr_x, tab_x, 0);
        jnienv->ReleaseFloatArrayElements(arr_y, tab_y, 0);
        jnienv->ReleaseFloatArrayElements(arr_z, tab_z, 0);

        float *tuple = ((SmartIDCore*)thiz)->authenticate(data_x, data_y, data_z);
        jfloatArray ret = jnienv->NewFloatArray(2);
        jnienv->SetFloatArrayRegion(ret, 0, 2, tuple);
        return ret;
    }

    JNIEXPORT void JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeClearPredictions(JNIEnv *jnienv, jobject object, jlong thiz);
    JNIEXPORT void JNICALL Java_br_unicamp_ic_recod_smartid_external_LibSmartID_nativeClearPredictions(JNIEnv *jnienv, jobject object, jlong thiz) {
        ((SmartIDCore*)thiz)->clearPredictions();
    }
}