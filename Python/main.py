import numpy as np
import pandas as pd
import os
from os.path import join
import glob
import cv2
import matplotlib.pyplot as plt
TEST_PATH = "input/test"

def ResizeWithAspectRatio(image, width=None, height=None, inter=cv2.INTER_AREA):
    dim = None
    (h, w) = image.shape[:2]

    if width is None and height is None:
        return image
    if width is None:
        r = height / float(h)
        dim = (int(w * r), height)
    else:
        r = width / float(w)
        dim = (width, int(h * r))

    return cv2.resize(image, dim, interpolation=inter)

test_ids = os.listdir(TEST_PATH)
test_image_paths = [glob.glob(join(TEST_PATH, test_id, "images","*"))[0] for test_id in test_ids]
image_path =  np.random.choice(test_image_paths)
img = cv2.imread(image_path, cv2.IMREAD_COLOR)
gray = cv2.cvtColor(img, cv2.COLOR_RGB2GRAY)
gray_blurred = cv2.blur(gray, (3, 3))


alpha = 1.1 # Contrast control (1.0-3.0)
beta = 0 # Brightness control (0-100)
adjusted = cv2.convertScaleAbs(gray_blurred, alpha=alpha, beta=beta)
# for i in range(len(adjusted)):
#     for j in range(len(adjusted)):
#         if(adjusted[i][j] != 255):
#             adjusted[i][j] = 0

# resize1 = ResizeWithAspectRatio(gray_blurred, width=700)
# cv2.imshow("Original", resize1)


detected_circles = cv2.HoughCircles(adjusted,cv2.HOUGH_GRADIENT, 0.2, 4, param1 = 35, 
               param2 = 23, minRadius = 2, maxRadius = 15) 

# plt.imshow(gray)
# plt.show()

print(len(detected_circles[0]))

if detected_circles is not None: 
  
    # Convert the circle parameters a, b and r to integers. 
    detected_circles = np.uint16(np.around(detected_circles)) 
  
    for pt in detected_circles[0, :]: 
        a, b, r = pt[0], pt[1], pt[2] 
  
        # Draw the circumference of the circle. 
        cv2.circle(adjusted, (a, b), r, (0, 255, 0), 2) 
  
        # Draw a small circle (of radius 1) to show the center. 
        cv2.circle(adjusted, (a, b), 1, (0, 0, 255), 3) 


resize = ResizeWithAspectRatio(adjusted, width=700)
cv2.imshow("Detected Circles", resize) 
cv2.waitKey(0) 

# if circles is not None:
# 	# convert the (x, y) coordinates and radius of the circles to integers
# 	circles = np.round(circles[0, :]).astype("int")
# 	# loop over the (x, y) coordinates and radius of the circles
# 	for (x, y, r) in circles:
# 		# draw the circle in the output image, then draw a rectangle
# 		# corresponding to the center of the circle
# 		cv2.circle(output, (x, y), r, (0, 255, 0), 4)
# 		cv2.rectangle(output, (x - 5, y - 5), (x + 5, y + 5), (0, 128, 255), -1)
# 	# show the output image
# 	cv2.imshow("output", np.hstack([tmp_image, output]))
# 	cv2.waitKey(0)




