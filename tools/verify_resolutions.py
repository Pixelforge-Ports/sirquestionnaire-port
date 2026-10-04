"""Run real game input tests at five display sizes using a local Java 17 runtime."""
import argparse, os, shutil, struct, subprocess, zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--java',type=Path,required=True);p.add_argument('--jdk',type=Path,required=True);p.add_argument('--game-jar',type=Path,required=True);p.add_argument('--seed-saves',type=Path);p.add_argument('--sizes',nargs='+',default=['640x480','720x480','720x720','1024x768','1280x720']);p.add_argument('--test-save',action='store_true');p.add_argument('--expect-resume',action='store_true');p.add_argument('--output-root',type=Path);p.add_argument('--interface',action='store_true',help='Check menus, quest text and inventory at native aspect ratios');p.add_argument('--host',type=Path,help='Use a separately compiled test host')
a=p.parse_args();java=a.java.resolve();game=a.game_jar.resolve()
if a.interface and (a.test_save or a.expect_resume or a.seed_saves):p.error('--interface uses fresh temporary saves; do not combine it with save/resume options')
smoke='InterfaceSmoke' if a.interface else 'GameplaySmoke'
host=a.host.resolve() if a.host else ROOT/'build/artifacts/sirquestionnaire-host.jar'
if not host.is_file() and not a.host:host=ROOT/'package/sirquestionnaire/runtime/sirquestionnaire-host.jar'
if not host.is_file():p.error('Host JAR not found: '+str(host))
classes=ROOT/'build/test-classes';classes.mkdir(parents=True,exist_ok=True)
compile_cp=ROOT/'build/test-compile-classpath'
compile_cp.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(game) as archive:
 for entry in archive.infolist():
  if entry.filename.endswith('.class'):
   target=(compile_cp/entry.filename).resolve();target.relative_to(compile_cp.resolve())
   target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(archive.read(entry))
cp=os.pathsep.join(map(str,[host,compile_cp]))
subprocess.run([str(a.jdk.resolve()/'bin'/('javac.exe' if os.name=='nt' else 'javac')),'--release','8','-Xlint:-options','-cp',cp,'-d',str(classes),str(ROOT/'tests'/(smoke+'.java'))],check=True)
cp=os.pathsep.join(map(str,[classes,host,game]))
for width,height in [tuple(map(int,size.split("x"))) for size in a.sizes]:
 out=(a.output_root.resolve() if a.output_root else ROOT/'build/resolutions')/f'{width}x{height}';out.mkdir(parents=True,exist_ok=True)
 saves=out/('interface-saves-'+__import__('uuid').uuid4().hex if a.interface else 'saves')
 if a.seed_saves and not saves.exists(): shutil.copytree(a.seed_saves,saves)
 command=[str(java),'-Xms32m','-Xmx256m','-XX:+UseSerialGC','-Xlog:class+load=info','-Dsirquestionnaire.hidden=true',f'-Dsirquestionnaire.width={width}',f'-Dsirquestionnaire.height={height}',f'-Dsirquestionnaire.saves={saves}',f'-Dsirquestionnaire.output={out}','-cp',cp,'org.portmaster.sirquestionnaire.'+smoke]
 if a.expect_resume: command.insert(1,'-Dsirquestionnaire.expectResume=true')
 if a.test_save: command.insert(1,'-Dsirquestionnaire.testSave=true')
 with (out/'run.log').open('w') as log: subprocess.run(command,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=360)
 log=(out/'run.log').read_text();assert ('UI_SMOKE_OK' if a.interface else 'GAMEPLAY_OK') in log
 if a.test_save: assert 'SAVE_READBACK_OK' in log
 if a.expect_resume: assert 'RESUME_DATA_FOUND' in log
 assert 'Exception:' not in log and 'Exception in thread' not in log, out
 assert 'com.codedisaster.steamworks.SteamAPI source:' not in log
 assert 'com.studiohartman.jamepad.ControllerManager source:' not in log
 assert 'com.orangepixel.questionnaire.desktop.EpicGames source:' not in log
 for name in (('menu.png','quests.png','inventory.png','gameplay.png') if a.interface else ('final.png',)):
  png=(out/name).read_bytes();assert struct.unpack('>II',png[16:24])==(width,height)
 assert any(p.is_file() for p in saves.rglob('*'))
 print(f'RESOLUTION_OK {width}x{height}',flush=True)
