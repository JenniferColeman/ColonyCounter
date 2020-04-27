import numpy as np
import pandas as pd
import os
from os.path import join
import glob
#import cv2
import matplotlib.pyplot as plt

TRAIN_PATH = "input/stage1_train"
TEST_PATH = "input/stage2_test_final"

trains_ids = os.listdir(TRAIN_PATH)
test_ids = os.listdir(TEST_PATH)

test_image_paths = [glob.glob(join(TEST_PATH, test_id, "images","*"))[0] for test_id in test_ids]

