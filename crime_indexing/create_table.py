import csv
reader = csv.DictReader(open('./dpd.csv'))

result = {}
for row in reader:
    for column, value in row.iteritems():
        result.setdefault(column, []).append(value)

sofc = result['STATEOFFENSEFILECLASS']
loc = result['LOCATION']

for x in range(len(loc)):
    sofc[x] = 30000 - int(sofc[x])

    loc[x] = loc[x].split()[-2]+' '+loc[x].split()[-1]
    loc[x] = loc[x].strip('()')
    st = [loc[x] , sofc[x]]
    # print(st)
    with open('BPData.csv','ab') as mycsvfile:
        temp = csv.writer(mycsvfile)
        temp.writerow(st)
