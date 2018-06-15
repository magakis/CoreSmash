package com.breakthecore.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.breakthecore.CoreSmash;
import com.breakthecore.UserAccount;
import com.breakthecore.ui.UIComponent;

public class ScoresScreen extends ScreenBase {
    private Stage stage;
    private Skin skin;
    private UIScoreTable uiScoreTable;

    public ScoresScreen(CoreSmash game) {
        super(game);
        setupStage();

        screenInputMultiplexer.addProcessor(new BackButtonInputHandler());
        screenInputMultiplexer.addProcessor(stage);
    }

    @Override
    public void show() {
        uiScoreTable.updateTable();
    }

    @Override
    public void render(float delta) {
        stage.act();
        stage.draw();
    }

    private void setupStage() {
        stage = new Stage(gameInstance.getUIViewport());
        skin = gameInstance.getSkin();
        uiScoreTable = new UIScoreTable();


        VerticalGroup verticalGroup = new VerticalGroup();
        verticalGroup.setFillParent(true);
        verticalGroup.addActor(new Label("Scores", skin, "h1"));
        verticalGroup.padTop(50);
        verticalGroup.addActor(uiScoreTable.getRoot());
        stage.addActor(verticalGroup);
    }


    private class BackButtonInputHandler extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                gameInstance.setPrevScreen();
            }
            return false;
        }
    }
    private class UIScoreTable implements UIComponent {
        Table root;
        Label lblScore[], lblTotalScore;

         UIScoreTable(){
             root = new Table();
             root.defaults().padRight(30).padBottom(20);
             root.pad(50);

             root.add(new Label("A/A",skin, "h4"));
             root.add(new Label("Difficulty",skin, "h4"));
             root.add(new Label("Score",skin, "h4")).row();

             lblScore = new Label[5];
             lblTotalScore = new Label("", skin, "comic_72bo");

             String style1st = "comic_72bo",
                     style2nd = "h4",
                     styledef = "h4";

             for (int i = 0; i < 5; ++i) {
                 String style = i == 0 ? style1st : i == 1 ? style2nd : styledef;
                 lblScore[i] = new Label("", skin, style);

                 root.add(new Label("#"+(i+1), skin, style));
                 root.add(lblScore[i]).row();
             }

             root.add(lblTotalScore).colspan(root.getColumns()).center().padTop(100).row();
         }

         void updateTable() {
             UserAccount user = gameInstance.getUserAccount();

             for (int i = 0; i < 5; ++i) {
                 lblTotalScore.setText("Total Score: " + user.getTotalScore());
             }
         }

        @Override
        public Group getRoot() {
            return root;
        }
    }
}
