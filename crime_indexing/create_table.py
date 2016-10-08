from urllib.request import urlopen
from urllib.parse import urlencode

# lat=42.444741336387835&long=-83.23036193847656
# lat=42.26968760326632&long=-82.86575317382812
obj = open("output.txt", "w")

for lat in range(4225, 4244, 2):
    for lng in range(-8323, -8287, 2):
        myParameters = {'lat': lat/100, 'long': lng/100, 'rad': 1000}
        print(lng/100)
        # params = urlencode(myParameters)
        # print(params)
        # f = urlopen('http://localhost:3000/crimes?'+params).read()
        # print(f)
        # obj.write(f)
        # print(myParameters)
