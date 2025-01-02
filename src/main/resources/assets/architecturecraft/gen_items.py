from pathlib import Path
import json

def main():
    # Get all the .json files in the models/item directory then filter for ones that are prefixed with "shape_"
    item_models = list(Path("models/item").glob("*.json"))
    item_models = [p for p in item_models if p.stem.startswith("shape_")]

    for model in item_models:
        print(f"Processing {model.stem}")
        item_path = Path("items") / f"{model.stem}.json"
        name = model.stem
        file_contents = {
            "model": {
                "type": "minecraft:model",
                "model": "architecturecraft:block/" + name
            }
        }
        item_path.write_text(json.dumps(file_contents, indent=4))



if __name__ == "__main__":
    main()