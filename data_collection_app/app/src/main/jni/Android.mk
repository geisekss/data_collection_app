LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := gsl
LOCAL_SRC_FILES := $(LOCAL_PATH)/gsl/lib/libgsl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := gslcblas
LOCAL_SRC_FILES := $(LOCAL_PATH)/gsl/lib/libgslcblas.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := opencv_java3
LOCAL_SRC_FILES := $(LOCAL_PATH)/opencv/lib/libopencv_java3.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES  := $(FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/gsl/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/opencv/include

LOCAL_LDLIBS += -llog -ldl
LOCAL_MODULE := smartid

LOCAL_SHARED_LIBRARIES := opencv_java3
LOCAL_STATIC_LIBRARIES := gsl gslcblas

include $(BUILD_SHARED_LIBRARY)