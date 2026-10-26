from PIL import Image, ImageDraw, ImageFilter
from pathlib import Path
import random, math
out=Path("app/src/main/assets")
out.mkdir(parents=True,exist_ok=True)
palettes=[
 ("cinema_space.jpg", (6,15,31),(40,81,120),(10,16,27)),
 ("cinema_desert.jpg",(28,14,30),(152,75,54),(27,21,31)),
 ("cinema_forest.jpg",(4,18,27),(48,110,91),(8,28,27)),
 ("cinema_urban.jpg",(16,10,39),(97,30,91),(12,13,37)),
 ("cinema_arctic.jpg",(15,33,54),(107,144,161),(17,41,58)),
 ("cinema_noir.jpg",(23,17,35),(95,59,74),(21,16,30))
]
for n,(name,top,mid,low) in enumerate(palettes):
 r=random.Random(n+35); w,h=1920,1080
 im=Image.new("RGB",(w,h))
 d=ImageDraw.Draw(im,"RGBA")
 for y in range(h):
  t=y/h
  mix=(min(1,t*1.6))
  a=tuple(round(top[k]*(1-mix)+mid[k]*mix) for k in range(3))
  d.line((0,y,w,y),fill=a,width=1)
 if n==0:
  cx,cy,rad=1455,300,290
  for k in range(110,0,-3):
   d.ellipse((cx-rad-k,cy-rad-k,cx+rad+k,cy+rad+k), fill=(112,176,226,int(1+(110-k)/30)))
  d.ellipse((cx-rad,cy-rad,cx+rad,cy+rad),fill=(66,110,157,250))
  for i in range(80):
   x,y=cx+r.randint(-rad,rad),cy+r.randint(-rad,rad)
   if (x-cx)**2+(y-cy)**2<rad**2:
    rr=r.randint(3,19);d.ellipse((x-rr,y-rr,x+rr,y+rr),fill=(38,78,119,r.randint(20,75)))
  d.arc((cx-rad,cy-rad,cx+rad,cy+rad),110,295,fill=(163,215,250,130),width=5)
 elif n==1:
  d.ellipse((1370,80,1860,570),fill=(250,151,91,80))
 elif n==4:
  d.ellipse((1350,90,1740,480),fill=(210,229,236,115))
 else:
  for i in range(100):
   x,y=r.randrange(w),r.randrange(650);rr=r.randint(1,2)
   d.ellipse((x,y,x+rr,y+rr),fill=(230,235,249,r.randint(40,170)))
 for depth in range(3):
  baseline=int(h*(.56+.10*depth))
  coords=[(-20,h),(-20,baseline)]
  for x in range(-20,w+70,90):
   coords.append((x,baseline+r.randint(-130,120)))
  coords.extend([(w+40,h),(-20,h)])
  fill=[(40,66,83,140),(24,39,53,215),(6,13,25,252)][depth] if n!=2 else [(22,72,59,145),(13,51,42,216),(4,23,27,255)][depth]
  d.polygon(coords,fill=fill)
 if n in (3,5):
  for i in range(70):
   x=r.randint(0,w);bh=r.randint(150,510)
   d.rectangle((x,885-bh,x+r.randint(15,65),885),fill=(8,12,25,235))
   for v in range(4):
    yy=885-bh+r.randint(8,max(9,bh-9));xx=x+r.randint(3,17)
    d.rectangle((xx,yy,xx+3,yy+8),fill=(241,127,171,r.randint(35,150)))
 if n in (0,1,4):
  x,y=1380,795
  d.ellipse((x-22,y-190,x+22,y-143),fill=(8,16,27,240))
  d.rounded_rectangle((x-38,y-151,x+40,y+10),radius=18,fill=(7,16,27,250))
  d.line((x-27,y+9,x-35,y+128),fill=(6,12,23,255),width=21)
  d.line((x+27,y+9,x+34,y+128),fill=(6,12,23,255),width=21)
  d.line((x-30,y-119,x-88,y-12),fill=(7,15,27,255),width=15)
  d.line((x+30,y-119,x+82,y-10),fill=(7,15,27,255),width=15)
 im.save(out/name,quality=84,optimize=True)
res=Path("app/src/main/res/drawable")
res.mkdir(parents=True,exist_ok=True)
im=Image.new("RGBA",(192,192),(19,27,45,255))
d=ImageDraw.Draw(im)
d.rounded_rectangle((12,12,180,180),radius=43,fill=(34,51,77,255))
d.ellipse((45,45,147,147),outline=(230,239,255,255),width=12)
d.polygon(((85,70),(85,123),(126,96)),fill=(250,250,253,255))
im.save(res/"icon.png")
print("Generated demo artwork and app icon")
