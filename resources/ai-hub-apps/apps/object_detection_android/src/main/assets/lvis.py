from ultralytics import YOLOWorld
import os

# 1. Load the model
model = YOLOWorld('yolov8s-worldv2.pt')

# 2. Path to labels
labels_path = './labels.txt'

# 3. Read and set classes
with open(labels_path, 'r') as f:
    custom_classes = [line.strip() for line in f.readlines() if line.strip()]

print(f"Baking {len(custom_classes)} travel items into the model...")
model.set_classes(custom_classes)

# 4. Export with opset=12 to fix the Einsum dimension error
model.export(format='tflite', imgsz=640, opset=12)