from pathlib import Path
from os import listdir
import json

def convert(unconverted_objson:Path, output_dir:Path):
    print('Converting ' + unconverted_objson.name)
    old_objson = json.loads(unconverted_objson.read_text())
    new_objson = {
        'faces': []
    }

    if 'bounds' in old_objson:
        new_objson['bounds'] = old_objson['bounds']

    if 'boxes' in old_objson:
        new_objson['boxes'] = old_objson['boxes']

    for face in old_objson['faces']:
        new_face = {
            'texture': face['texture'],
            'vertices': [],
            'triangles': []
        }
        for vert in face['vertices']:
            new_face['vertices'].append(
                {
                    'pos': vert[0:3],
                    'normal': vert[3:6],
                    'uv': vert[6:8]
                }
            )
        for tri in face['triangles']:
            new_face['triangles'].append(
                {
                    'vertices': tri
                }
            )
        new_objson['faces'].append(new_face)
    output = Path(output_dir, unconverted_objson.name)
    print('Writing ' + str(output))
    output.touch()
    output.write_text(json.dumps(new_objson, indent=4))


output_dir = Path('converted')
output_dir.mkdir(exist_ok=True, parents=True)

for l in listdir():
    if '.objson' in l:
        convert(Path(l), output_dir)

